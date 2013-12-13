resolvers += Resolver.url("bintray-sbt-plugin-releases",
             url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

resolvers ++= Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "sbt-plugin-releases2" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/",
  "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"
)

addSbtPlugin("org.scala-sbt" % "sbt-closure" % "0.1.4")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.5")

addSbtPlugin("me.lessis" % "less-sbt" % "0.2.2")

// addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.6")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.2.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")


