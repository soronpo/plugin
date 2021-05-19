val dottyVersion = "3.0.0-RC3"
val libVersion = "0.1.0"
val org = "org.mycompany"

lazy val plugin = project
  .settings(
    name := "scala-counter-plugin",
    organization := org,
    version := libVersion,

    scalaVersion := dottyVersion,
    crossTarget := target.value / s"scala-${scalaVersion.value}", // workaround for https://github.com/sbt/sbt/issues/5097
    crossVersion := CrossVersion.full,

    libraryDependencies += "org.scala-lang" %% "scala3-compiler" % dottyVersion % "provided"
  )

lazy val runtime = project
  .settings(
    name := "scala-counter-runtime",
    organization := "org.mycompany",
    version := libVersion,

    scalaVersion := dottyVersion
  )


lazy val hello = project
  .settings(
    name := "hello",
    version := "0.1.0",
    scalaVersion := dottyVersion,

    Compile / scalacOptions ++= {
      val jar = (plugin / Compile / packageBin).value
      Seq(
        s"-Xplugin:${jar.getAbsolutePath}",
        s"-Jdummy=${jar.lastModified}"
      ) //borrowed from bm4
    },
    // scalacOptions ++= Seq(
    //   "-Xprint:pickler",
    //   "-Xprint:MetaContext",
    // ),
  ).dependsOn(plugin, runtime)


lazy val root = project
  .aggregate(plugin, runtime, hello)
