-keepattributes SourceFile, LineNumberTable

-keepattributes *Annotation*

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-renamesourcefileattribute SourceFile

-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
