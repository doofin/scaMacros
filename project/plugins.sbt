resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases",
  "scala-integration" at "https://scala-ci.typesafe.com/artifactory/scala-integration/"
)

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "0.6.24")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.5.0")
addSbtPlugin("io.get-coursier"    % "sbt-coursier"             % "1.0.3")
