val dottyVersion = "3.0.0-RC3"
val libVersion = "0.1.0"
val org = "org.mycompany"

autoCompilerPlugins := true

lazy val plugin = project
  .settings(
    name := "scala-counter-plugin",
    organization := org,
    version := libVersion,

    scalaVersion := dottyVersion,

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

    scalacOptions ++= Seq(
      "-P:counter:hello/counter.yml",
      "-Xprint:pickler",
      "-Xprint:MetaContext"
    ),

    libraryDependencies += "org.mycompany" %% "scala-counter-runtime" % "0.1.0",
    libraryDependencies += compilerPlugin("org.mycompany" %% "scala-counter-plugin" % "0.1.0")
  ).dependsOn(runtime)


lazy val root = project
  .aggregate(plugin, runtime)
