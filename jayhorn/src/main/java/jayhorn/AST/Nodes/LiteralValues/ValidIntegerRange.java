package jayhorn.AST.Nodes.LiteralValues;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

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
     * Returns the underlying RangeSet.
     */
    public RangeSet<Integer> getRangeSet() {
        return rangeSet;
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
