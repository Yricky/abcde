package com.yricky.oh.abclen

import me.yricky.oh.OffsetRange


class TaggedUsageMap {
    private val ranges = mutableListOf<Pair<OffsetRange, String>>()
    /**
     * 获取目前已经被使用的区间范围以及标签
     */
    fun getUsedRanges(): List<Pair<OffsetRange, String>> {
        return ranges.toList()
    }
    /**
     * 将当前区间标为已经使用，如果这个range和已有range首尾相连且tag相同，则将它们合并为一个。
     * 如果与已有区间重叠，则直接抛出异常。
     */
    fun markAsUsed(range: OffsetRange, tag: String) {
        // 空列表直接添加
        if (ranges.isEmpty()) {
            ranges.add(range to tag)
            return
        }
        // 二分查找插入位置
        val index = ranges.binarySearch { (r, _) -> r.start.compareTo(range.start) }
        val insertionPoint = if (index >= 0) {
            // 存在相同start的区间，必然重叠
            throw IllegalArgumentException("Overlap with existing range:${range.start}")
        } else {
            -index - 1 // 计算插入位置
        }
        // 检查与前一个区间重叠
        if (insertionPoint > 0) {
            val (prevRange, pTag) = ranges[insertionPoint - 1]
            if (prevRange.endExclusive > range.start) {
                throw IllegalArgumentException("Overlap with previous range:${prevRange}${pTag} and ${range}${tag}")
            }
        }
        // 检查与后一个区间重叠
        if (insertionPoint < ranges.size) {
            val (nextRange, _) = ranges[insertionPoint]
            if (range.endExclusive > nextRange.start) {
                throw IllegalArgumentException("Overlap with next range")
            }
        }
        // 初始化合并参数
        var mergedRange = range
        var leftCount = 0
        var rightCount = 0
        // 向左合并相邻同标签区间
        var leftIndex = insertionPoint - 1
        while (leftIndex >= 0) {
            val (leftRange, leftTag) = ranges[leftIndex]
            if (leftRange.endExclusive == mergedRange.start && leftTag == tag) {
                mergedRange = OffsetRange(leftRange.start, mergedRange.endExclusive)
                leftCount++
                leftIndex--
            } else {
                break
            }
        }
        // 向右合并相邻同标签区间
        var rightIndex = insertionPoint
        while (rightIndex < ranges.size) {
            val (rightRange, rightTag) = ranges[rightIndex]
            if (mergedRange.endExclusive == rightRange.start && rightTag == tag) {
                mergedRange = OffsetRange(mergedRange.start, rightRange.endExclusive)
                rightCount++
                rightIndex++
            } else {
                break
            }
        }
        // 计算删除范围
        val removeStart = insertionPoint - leftCount
        val removeEnd = insertionPoint + rightCount - 1
        // 删除被合并的区间
        if (removeStart <= removeEnd) {
            ranges.subList(removeStart, removeEnd + 1).clear()
        }
        // 插入合并后的新区间
        ranges.add(removeStart, mergedRange to tag)
    }
}

/**
 * 合并相邻且标签在同一标签组内的区间，返回的区间的标签集合是被合并的区间标签的并
 * @param ranges 有序不重叠的带标签区间列表
 * @param tagGroups 标签组列表，一个标签只会出现在一个标签组中
 */
fun mergeTaggedRanges(ranges: List<Pair<OffsetRange, String>>, tagGroups: List<Set<String>>): List<Pair<OffsetRange, Set<String>>> {
    if (ranges.isEmpty()) return emptyList()

    // 构建标签到组的映射（使用组对象本身作为值）
    val tagToGroup = mutableMapOf<String, Set<String>>()
    for (group in tagGroups) {
        for (tag in group) {
            tagToGroup[tag] = group
        }
    }

    val result = mutableListOf<Pair<OffsetRange, Set<String>>>()
    var currentRange = ranges[0].first
    var currentTags = mutableSetOf(ranges[0].second)
    var currentGroup: Set<String>? = tagToGroup[ranges[0].second]

    for (i in 1 until ranges.size) {
        val (nextRange, nextTag) = ranges[i]
        val nextGroup = tagToGroup[nextTag]

        // 检查是否相邻且同组
        val adjacent = currentRange.endExclusive == nextRange.start
        val sameGroup = currentGroup != null && nextGroup != null && currentGroup === nextGroup

        if (adjacent && sameGroup) {
            // 合并区间
            currentRange = OffsetRange(currentRange.start, nextRange.endExclusive)
            currentTags.add(nextTag)
        } else {
            // 保存当前合并结果
            result.add(currentRange to currentTags.toSet())

            // 开始新的合并
            currentRange = nextRange
            currentTags = mutableSetOf(nextTag)
            currentGroup = nextGroup
        }
    }

    // 添加最后一个合并结果
    result.add(currentRange to currentTags.toSet())

    return result
}

class RangeUsageMap {

    private val usedRanges = mutableListOf<OffsetRange>()

    /**
     * 获取目前已经被使用的区间范围，返回的区间不会重复，且不会首尾相连
     */
    fun getUsedRanges(): List<OffsetRange> {
        return usedRanges
    }

    /**
     * 将当前区间标为已经使用
     */
    fun markAsUsed(range: OffsetRange) {
        if (usedRanges.isEmpty()) {
            usedRanges.add(range)
            return
        }

        var startIndex = -1
        var endIndex = -1
        var minStart = range.start
        var maxEnd = range.endExclusive

        var i = 0
        while (i < usedRanges.size) {
            val existing = usedRanges[i]
            // 检查重叠或相邻 (existing与range)
            if (existing.start <= range.endExclusive && range.start <= existing.endExclusive) {
                if (startIndex == -1) startIndex = i
                endIndex = i
                minStart = minOf(minStart, existing.start)
                maxEnd = maxOf(maxEnd, existing.endExclusive)
            } else if (existing.endExclusive < range.start) {
                // 当前区间在目标左侧，继续
            } else if (existing.start > range.endExclusive) {
                // 当前区间在目标右侧，后续区间也都在右侧，跳出
                break
            }
            i++
        }

        if (startIndex != -1) {
            // 删除所有重叠区间
            usedRanges.subList(startIndex, endIndex + 1).clear()
            // 插入合并后的新区间
            usedRanges.add(startIndex, OffsetRange(minStart, maxEnd))
        } else {
            // 无重叠，找到插入位置（保持有序）
            val insertIndex = usedRanges.indexOfFirst { it.start > range.start }
            if (insertIndex == -1) {
                usedRanges.add(range) // 插入末尾
            } else {
                usedRanges.add(insertIndex, range)
            }
        }
    }
}
