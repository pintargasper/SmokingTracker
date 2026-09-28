-keepclassmembers enum com.gasperpintar.smokingtracker.type.AchievementCategory { *; }

-keep class com.gasperpintar.smokingtracker.database.model.AchievementJsonEntry { *; }

-keep class org.apache.poi.schemas.ooxml.system.ooxml.TypeSystemHolder { *; }

-keep class org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.impl.CTPropertiesImpl { <init>(...); }
-keep class org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.impl.PropertiesDocumentImpl { <init>(...); }

-keep class org.openxmlformats.schemas.officeDocument.x2006.customProperties.impl.PropertiesDocumentImpl { <init>(...); }
-keep class org.openxmlformats.schemas.officeDocument.x2006.customProperties.impl.CTPropertiesImpl { <init>(...); }

-keep class org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.impl.STXstringImpl { <init>(...); }

-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.** { <init>(...); }

-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType$Enum { *; }

-dontwarn aQute.bnd.annotation.baseline.BaselineIgnore
-dontwarn aQute.bnd.annotation.spi.ServiceConsumer
-dontwarn aQute.bnd.annotation.spi.ServiceProvider

-dontwarn com.github.luben.zstd.ZstdInputStream

-dontwarn edu.umd.cs.findbugs.annotations.Nullable
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings

-dontwarn java.awt.**
-dontwarn javax.xml.stream.**

-dontwarn net.sf.saxon.**
-dontwarn org.osgi.framework.**

-dontwarn org.openxmlformats.schemas.**