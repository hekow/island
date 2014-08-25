package eu.ace_design.island.viewer

import java.io.{PrintWriter, File}

import eu.ace_design.island.geom.{Point, Mesh, VertexRegistry}
import eu.ace_design.island.map.{PropertySet, IslandMap}

/**
 * Create 3d mesh using the OBJ file format [http://en.wikipedia.org/wiki/Wavefront_.obj_file]
 */
class OBJViewer extends Viewer {

  override val extension: String = "obj"
  override val mimeType: String = "application/octet-stream"

  /**
   * Apply this transformation to a given map
   * @param m the map one wants to visualize
   * @return a File containing the associated representation
   */
  override def apply(m: IslandMap): File = {
    info("Building an OBJ file")
    val header =
      """####
         |## Island 3d model (OBJ compliant file)
         |## Automatically generated by the [OBJViewer] transformation process
         |####
      """.stripMargin
    val vertices: Seq[String] = buildVertices(m)
    val polygons: Seq[String] = buildPolygons(m)

    val result = initOutput
    val writer = new PrintWriter(result, "UTF8")
    writer.write(header)
    writer.write(s"\n\n####\n## Vertices registry\n####\n${vertices.mkString("\n")}")
    writer.write(s"\n\n####\n## Polygons registry\n####\n${polygons.mkString("\n")}")
    writer.close()
    info("done")
    result
  }

  /**
   * Build the vertices registry (sequence of vertex description in plain text)
   * A vertex is represented as "v $x $y $z"
   * @param m the map containing the vertex registry used to store the ≠ vertices
   * @return a sequence of vertex description
   */
  private def buildVertices(m: IslandMap): Seq[String] = {
    info("Building vertices index")
    ViewerHelpers.buildVertices(m) map { c => s"v ${c._1} ${c._2} ${c._3}" }
  }

  /**
   * Transform the faces stored in the mesh into their OBJ representation
   * An OBJ face is a sequence of 3+ vertices references (starting at 1): "f v1 ... vN"
   * Specifying normals is optional (thus not done here)
   * @param m the map storing the faces, edges and vertices
   * @return a sequence of face description
   */
  private def buildPolygons(m: IslandMap): Seq[String] = {
    info("Building faces index")
    // OBJ index starts at 1 instead of 0 => adding one to each components before producing the string.
    ViewerHelpers.buildFaces(m) map { _ map { _ + 1} } map { f => s"f ${f.mkString(" ")}" }
  }


}
