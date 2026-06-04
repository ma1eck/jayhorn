package jayhorn.phaseOneParser.LiteralValues;

import com.google.common.collect.*;

public class ValidIntegerRange {

    private final RangeSet<Integer> rangeSet;

    public ValidIntegerRange() {
        this.rangeSet = TreeRangeSet.create();
        // Set default range to [-inf, +inf]
        this.rangeSet.add(Range.all());
    }
    public ValidIntegerRange(int point) {
        this.rangeSet = TreeRangeSet.create();
        this.rangeSet.add(Range.closed(point, point));
    }
    public ValidIntegerRange(int start, int end) {
        this.rangeSet = TreeRangeSet.create();
        this.rangeSet.add(Range.closed(start, end));
    }

    public Integer getMinValue() {
        if (rangeSet.isEmpty()) {
            return null;
        }
        Range<Integer> span = rangeSet.span();
        if (!span.hasLowerBound()) {
            return Integer.MIN_VALUE;
        }
        return span.lowerBoundType() == com.google.common.collect.BoundType.CLOSED
                ? span.lowerEndpoint()
                : span.lowerEndpoint() + 1;
    }

    public Integer getMaxValue() {
        if (rangeSet.isEmpty()) {
            return null;
        }
        Range<Integer> span = rangeSet.span();
        if (!span.hasUpperBound()) {
            return Integer.MAX_VALUE;
        }
        return span.upperBoundType() == com.google.common.collect.BoundType.CLOSED
                ? span.upperEndpoint()
                : span.upperEndpoint() - 1;
    }


    /**
     * UNION: Adds a closed interval [start, end] to the valid ranges.
     * Overlapping intervals are automatically merged.
     */
    public void union(int start, int end) {
        rangeSet.add(Range.closed(start, end));
    }

    /**
     * UNION: Merges another RangeSet into this one.
     */
    public void union(RangeSet<Integer> otherRanges) {
        rangeSet.addAll(otherRanges);
    }

    /**
     * INTERSECTION: Restricts the valid ranges to ONLY those that overlap
     * with the closed interval [start, end].
     */
    public void intersect(int start, int end) {
        // To intersect with [start, end], we remove everything outside of it.
        rangeSet.remove(Range.lessThan(start));
        rangeSet.remove(Range.greaterThan(end));
    }

    /**
     * INTERSECTION: Restricts the valid ranges to ONLY those that overlap
     * with the provided RangeSet.
     */
    public void intersect(RangeSet<Integer> otherRanges) {
        // Intersecting is the same as removing the complement (everything NOT in the other set)
        rangeSet.removeAll(otherRanges.complement());
    }

    /**
     * EXCLUDE: Removes a closed interval [start, end] from the valid ranges.
     */
    public void exclude(int start, int end) {
        rangeSet.remove(Range.closed(start, end));
    }

    /**
     * EXCLUDE: Removes all ranges present in another ValidIntegerRange.
     */
    public void exclude(ValidIntegerRange other) {
        rangeSet.removeAll(other.getRangeSet());
    }

    /**
     * EXCLUDE: Removes all ranges present in the provided RangeSet.
     */
    public void exclude(RangeSet<Integer> otherRanges) {
        rangeSet.removeAll(otherRanges);
    }

    /**
     * Checks if a specific integer is currently within the valid ranges.
     */
    public boolean contains(int value) {
        return rangeSet.contains(value);
    }

    /**
     * Checks if this range has any overlap with another ValidIntegerRange.
     * Returns true if there is at least one common value.
     */
    public boolean hasOverlap(ValidIntegerRange other) {
        for (Range<Integer> thisRange : this.rangeSet.asRanges()) {
            for (Range<Integer> otherRange : other.rangeSet.asRanges()) {
                if (thisRange.isConnected(otherRange) && !thisRange.intersection(otherRange).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the valid range represents exactly one single integer value.
     * Returns the integer value if true, or null otherwise.
     */
    public Integer getSingleValue() {
        if (rangeSet.asRanges().size() == 1) {
            Range<Integer> range = rangeSet.asRanges().iterator().next();

            // The range must be bounded on both sides to represent a single finite value
            if (range.hasLowerBound() && range.hasUpperBound()) {
                // ContiguousSet evaluates the actual discrete integers in the range
                ContiguousSet<Integer> discreteSet = ContiguousSet.create(range, DiscreteDomain.integers());

                if (discreteSet.size() == 1) {
                    return discreteSet.first();
                }
            }
        }
        return null; // Not a single value (either empty, multiple values, or unbounded)
    }

    /**
     * Returns true if the valid range represents exactly one single integer value, false otherwise.
     */
    public boolean isSingleValue() {
        return getSingleValue() != null;
    }


    /**
     * Returns the underlying RangeSet.
     */
    public RangeSet<Integer> getRangeSet() {
        return rangeSet;
    }

    public ValidIntegerRange copy() {
        ValidIntegerRange clone = new ValidIntegerRange();
        // Clear the default [-inf, +inf] range added by the default constructor
        clone.rangeSet.clear();
        clone.rangeSet.addAll(this.rangeSet);
        return clone;
    }
    @Override
    public String toString() {
        return rangeSet.toString();
    }

    // --- Example Usage ---
    public static void main(String[] args) {
        ValidIntegerRange validRange = new ValidIntegerRange();
        System.out.println("Initial: " + validRange); // Initial: {(-∞..+∞)}

        // Intersect with [0, 100]
        validRange.intersect(0, 100);
        System.out.println("After Intersect [0, 100]: " + validRange); // {[0..100]}

        // Remove a chunk by intersecting with two distinct intervals
        RangeSet<Integer> subset = TreeRangeSet.create();
        subset.add(Range.closed(10, 20));
        subset.add(Range.closed(80, 90));

        validRange.intersect(subset);
        System.out.println("After Intersect with subset: " + validRange); // {[10..20], [80..90]}

        // Union with [15, 85] (this will bridge the gap and merge them all)
        validRange.union(15, 85);
        System.out.println("After Union [15, 85]: " + validRange); // {[10..90]}
    }
}
