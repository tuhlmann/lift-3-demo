resolvers ++= Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "sbt-plugin-releases2" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"
)

addSbtPlugin("org.scala-sbt" % "sbt-closure" % "0.1.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.2")

addSbtPlugin("me.lessis" % "less-sbt" % "0.1.10")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.6")

libraryDependencies <+= sbtVersion(v => v match {
case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
case "0.11.3" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.3-0.2.11.1"
case x if (x.startsWith("0.12")) => "com.github.siasia" %% "xsbt-web-plugin" % "0.12.0-0.2.11.1"
})

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.2")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0")
