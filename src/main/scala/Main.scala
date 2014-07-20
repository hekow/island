import eu.ace_design.island.geom._
import eu.ace_design.island.viewer._
import eu.ace_design.island.util.Log


object Main extends App with Log {

  final val OUTPUT_FILE = "./map.pdf"
  final val MAP_SIZE = 2048
  final val NB_FACES = 2000

  logger.info("Starting the map generation process")

  // Randomly generate the sites used to generate the map
  val generator = new RelaxedRandomGrid(MAP_SIZE)
  val sites = generator(NB_FACES)

  // Instantiate a builder, and process the random sites to create a mesh
  val builder = new MeshBuilder(MAP_SIZE)
  val mesh = builder(sites)

  logger.info("End of the map generation process")

  logger.info("Starting the transformation into PDF")
  val transformer = new PDFViewer()
  val result = transformer(mesh)
  result.renameTo(new java.io.File(OUTPUT_FILE))

  logger.info("PDF file generated!")
}