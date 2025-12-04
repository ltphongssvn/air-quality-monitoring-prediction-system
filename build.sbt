// air-quality-monitoring-prediction-system/build.sbt
name := "air-quality-monitoring-prediction-system"
version := "0.1.0"
scalaVersion := "2.12.18"

// zipHomework task - creates scalaHomework.zip at project root
lazy val zipHomework = taskKey[Unit]("Create scalaHomework.zip for grading submission")

zipHomework := {
  import java.io.{File, FileInputStream, FileOutputStream}
  import java.util.zip.{ZipEntry, ZipOutputStream}
  
  val baseDir = baseDirectory.value
  val zipFile = baseDir / "scalaHomework.zip"
  
  // Directories to include
  val includeDirs = Seq("backend", "spark-jobs", "frontend", "k8s", "docs")
  // Files to include from root
  val includeFiles = Seq("docker-compose.yml", "README.md", ".env.example")
  
  val zip = new ZipOutputStream(new FileOutputStream(zipFile))
  
  def addFileToZip(file: File, entryName: String): Unit = {
    if (file.isFile && !file.getName.endsWith(".class") && !file.getName.endsWith(".jar") && !file.getName.equals(".gitkeep")) {
      zip.putNextEntry(new ZipEntry(entryName))
      val in = new FileInputStream(file)
      val buffer = new Array[Byte](1024)
      var len = in.read(buffer)
      while (len > 0) {
        zip.write(buffer, 0, len)
        len = in.read(buffer)
      }
      in.close()
      zip.closeEntry()
    }
  }
  
  def addDirToZip(dir: File, parentPath: String): Unit = {
    if (dir.isDirectory && !dir.getName.startsWith(".") && 
        dir.getName != "target" && dir.getName != "node_modules") {
      dir.listFiles.foreach { file =>
        val entryPath = if (parentPath.isEmpty) file.getName else s"$parentPath/${file.getName}"
        if (file.isDirectory) addDirToZip(file, entryPath)
        else addFileToZip(file, entryPath)
      }
    }
  }
  
  // Add directories
  includeDirs.foreach { dirName =>
    val dir = baseDir / dirName
    if (dir.exists) addDirToZip(dir, dirName)
  }
  
  // Add root files
  includeFiles.foreach { fileName =>
    val file = baseDir / fileName
    if (file.exists) addFileToZip(file, fileName)
  }
  
  zip.close()
  println(s"Created: ${zipFile.getAbsolutePath}")
}
