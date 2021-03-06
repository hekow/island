package eu.ace_design.island.map.processes

import eu.ace_design.island.geom.Face
import eu.ace_design.island.map._
import eu.ace_design.island.util.{LogSilos, Logger}

/**
 * A face is annotated as Water if it involves a number of vertices located in a water area (according to a given
 * IslandShape) greater than a given threshold. For each vertex, the process decides if it is water or not based on
 * the given shape. Then, it applies for each face a simple threshold function: if more than $threshold% of the vertices
 * involved in this face are water, then it is water.
 *
 * @param shape the IslandShape used for this Island
 * @param threshold the threshold (a percentage, in [0,100]) to decide if a face is a water one
 *
 * Pre-condition:
 *   - None. Actually usually the first process used to build an island
 *
 * Post-conditions:
 *   - Faces are identified by "IsWater(b)", where b in {true, false}.
 *
 */
case class IdentifyWaterArea(shape: IslandShape, threshold: Int) extends Process with Logger {
  require(threshold >= 0,   "threshold must be in [0,100]")
  require(threshold <= 100, "threshold must be in [0,100]")

  override def apply(m: IslandMap): IslandMap = {
    info("Creating the shape")
    val isWaterVertex = shape.isWater _
    val pRefs = m.findVertexRefsWith(isWaterVertex) // Find all the vertices matching the given shape

    info("Annotating faces")
    val isWaterFace: Face => Boolean = { f =>
      val vertices = m.cornerRefs(f)
      val waterVertices = vertices filter { r => pRefs.contains(r)}
      (waterVertices.size.toFloat / vertices.size) * 100 > threshold
    }
    val waterFaceRefs = m.findFaceRefsWith(isWaterFace)
    val landFaceRefs = m.faceRefs diff waterFaceRefs
    debug("Faces tagged as water: " + waterFaceRefs.toSeq.sorted.mkString("(", ",", ")"))
    debug("Faces tagged as land: " + landFaceRefs.toSeq.sorted.mkString("(", ",", ")"))
    val fProps = m.faceProps bulkAdd (waterFaceRefs -> IsWater()) bulkAdd (landFaceRefs -> !IsWater())

    m.copy(faceProps = fProps)
  }
}

/**
 * Considering that faces are annotated as !IsWater thanks to a threshold, this process aligns the involved vertices.
 * As a consequence, land faces involves only vertices considered as land, and water vertices (all the others used as
 * face corners) are annotated as water.
 *
 * Pre-condition:
 *   - Faces are annotated as "IsWater(b)", where b in {true, false}
 *
 * Post-conditions:
 *   - Vertices involved in the borders of a land face are always considered as land (=> !IsWater())
 *   - Vertices involved in face centers are aligned with their related face
 *   - All vertices in the map are annotated as water OR land
 */
object AlignVertexWaterBasedOnFaces extends Process  {

  override def apply(m: IslandMap): IslandMap = {
    info("Annotating land and water vertices based on faces tags")
    // Interesting vertices are all the vertices not used as the center of a given face.
    val exceptCenters = m.vertexRefs diff (m.faces map { _.center })
    val landFaceRefs = m.findFacesWith(Set(!IsWater())) map { f => m.faceRef(f) }
    // Land vertices are the one involved in a land faces. All the other are water
    val landVertices = landFaceRefs flatMap { idx: Int => m.cornerRefs(m.face(idx)) }
    // Others are water (excepting the centers of the faces)
    val waterVertices = exceptCenters diff landVertices
    debug("Vertices tagged as water: " + waterVertices.toSeq.sorted.mkString("(", ",", ")"))
    debug("Vertices tagged as land: " + landVertices.toSeq.sorted.mkString("(", ",", ")"))
    val vProps = m.vertexProps bulkAdd (waterVertices -> IsWater()) bulkAdd (landVertices -> !IsWater())
    // aligning face centers with the surrounding face
    val finalPSet = (vProps /: m.faces) { (acc, f) =>
      val fVal =  m.faceProps.getValue(m.faceRef(f), IsWater()) // the value for this face
      acc + (f.center -> IsWater(fVal))
    }
    m.copy(vertexProps = finalPSet)
  }
}
