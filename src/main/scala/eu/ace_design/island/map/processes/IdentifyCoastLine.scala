package eu.ace_design.island.map.processes

import eu.ace_design.island.map._
import eu.ace_design.island.util.{LogSilos, Logger}

/**
 * A face is considered as a coast if it is a land one which is connected to at least one ocean face. A vertex is a
 * coast one if it is involved in both a land face and an ocean one.
 *
 * Pre-conditions:
 *   - Faces are annotated as IsWater(b) (b in {true, false}) and WaterKind(OCEAN)
 *   - Vertices are aligned with the face annotations
 *
 * Post-conditions:
 *   - Faces identified as coast ones are annotated as IsCoast(true). (others not impacted)
 *   - Vertices identified as coast are annotated as IsCoast(true). (others not impacted)
 *
 */
object IdentifyCoastLine extends Process {

  override def apply(m: IslandMap): IslandMap = {
    info("Annotating faces")
    val finder = m.faceProps.project(m.mesh.faces) _
    val oceans = finder(Set(WaterKind(ExistingWaterKind.OCEAN))) map { m.mesh.faces(_).get }
    val land = finder(Set(!IsWater()))
    val coast = land filter { f => (f.neighbors.get & oceans).nonEmpty } map { m.mesh.faces(_).get }

    debug("Faces tagged as coastline: " + coast.toSeq.sorted.mkString("(",",",")"))
    val fProps = m.faceProps bulkAdd (coast -> IsCoast())

    info("Annotating vertices")
    // coast vertices are involved in both coast and ocean faces
    val verticesInvolvedInOceanFaces = (oceans map { r => m.mesh.faces(r).vertices(m.mesh.edges)  }).flatten
    val verticesInvolvedInCoastFaces = (coast map  { r => m.mesh.faces(r).vertices(m.mesh.edges)  }).flatten
    val coastVertices = verticesInvolvedInCoastFaces & verticesInvolvedInOceanFaces
    debug("Vertices tagged as coastline: " + coastVertices.toSeq.sorted.mkString("(",",",")"))
    val vProps = m.vertexProps bulkAdd (coastVertices -> IsCoast())

    m.copy(faceProps = fProps, vertexProps = vProps)
  }
}
