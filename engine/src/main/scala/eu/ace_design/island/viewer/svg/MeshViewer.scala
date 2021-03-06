package eu.ace_design.island.viewer.svg

import java.awt.geom.Line2D
import java.awt.{BasicStroke, Graphics2D}

import eu.ace_design.island.map._
import eu.ace_design.island.stdlib.Colors
import Colors._

/**
 * The MeshViewer is used to display the mesh that is under the map. It differentiates oceans, lakes and land, and
 * focuses on the mesh relationship (faces borders, centers, corners and neighborhood).
 *
 * Pre-condition: Faces must be annotated with WaterKind(). Vertices with IsWater()
 */
object MeshViewer extends SVGViewer {

  protected def draw(m: IslandMap, g: Graphics2D): Unit = {
    m.faceRefs foreach { ref =>
      drawAPolygon(ref, m, g)
      drawCenters(ref, m, g)
      drawCorners(ref, m, g)
      drawNeighbors(ref, m, g)
    }

  }

  /**
   * Draw a face as a polygon.
   *@param idx the index of the face to draw
   * @param map the map used as a reference
   * @param g the graphics2D object used to paint
   */
  private def drawAPolygon(idx: Int, map: IslandMap, g: Graphics2D): Unit = {
    val path = buildPath(idx, map)
    g.setStroke(new BasicStroke(1f))
    g.draw(path)
    try {
      val color = if (map.faceProps.check(idx, WaterKind(ExistingWaterKind.OCEAN)))
        Colors.DARK_BLUE
      else if (map.faceProps.check(idx, WaterKind(ExistingWaterKind.LAKE)))
        Colors.MEDIUM_BLUE
      else Colors.WHITE
      g.setColor(color)
      g.fill(path)

    }  catch { case e: Exception => } // UGLY, DON'T DO THAT AT HOME. OK AS THIS VIEWER IS ONLY USED FOR DEBUG
  }


  /**
   * Draw the center of each face as a single black point (width: 3). Mainly used for explanation purpose
   * @param idx the index of the face to draw
   * @param map the map used as a reference
   * @param g the graphics2D object used to paint
   */
  private def drawCenters(idx: Int, map: IslandMap, g: Graphics2D) {
    val f = map.face(idx)
    g.setColor(LIGHT_GREY)
    g.setStroke(new BasicStroke(2))
    val center = map.vertex(f.center)
    g.draw(new Line2D.Double(center.x, center.y,center.x, center.y))
  }

  /**
   * Draw the neighborhood relationship as gray lines between faces' centers. Mainly used for explanation purposes
   * @param idx the index of the face to draw
   * @param map the map used as a reference
   * @param g the graphics2D object used to paint
   */
  private def drawNeighbors(idx: Int, map: IslandMap, g: Graphics2D) {
    val f = map.face(idx)
    val center = map.vertex(f.center)
    g.setColor(LIGHT_GREY)
    g.setStroke(new BasicStroke(0.05f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, Array{4.0f}, 0.0f))
    f.neighbors match {
      case None =>
      case Some(refs) => refs foreach { idx =>
        val p = map.vertex(map.face(idx).center)
        g.draw(new Line2D.Double(center.x, center.y, p.x, p.y))
      }
    }
  }

  /**
   * Draw the corners of each face, coloring water corners in blue and land one in black.
   * @param idx the index of the face to draw
   * @param map the map used as a reference
   * @param g the graphics2D object used to paint
   */
  private def drawCorners(idx: Int, map: IslandMap, g: Graphics2D) {
    val f = map.face(idx)
    g.setStroke(new BasicStroke(1))
    map.cornerRefs(f) foreach { ref =>
      if(map.vertexProps.check(ref, IsWater()))
        g.setColor(DARK_BLUE)
      else
        g.setColor(BLACK)
      val p = map.vertex(ref)
      g.draw(new Line2D.Double(p.x, p.y,p.x, p.y))
    }
  }

}
