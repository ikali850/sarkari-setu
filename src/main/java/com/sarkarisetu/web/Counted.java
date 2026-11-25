package com.sarkarisetu.web;

/** A label with a number and a bar width (for the dashboard chart). */
public class Counted {
    private final String label; private final long count; private final int percent;
    public Counted(String label, long count, int percent) { this.label = label; this.count = count; this.percent = percent; }
    public String getLabel() { return label; } public long getCount() { return count; } public int getPercent() { return percent; }
}
