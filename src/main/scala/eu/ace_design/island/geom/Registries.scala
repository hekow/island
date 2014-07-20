package eu.ace_design.island.geom

/**
 * This trait represents the common behavior for geometrical registries
 * @tparam T the geometrical element stored in the registry (e.g., Point, Edge, Face)
 */
trait Registry[T] {

  protected type InternalStorage = Map[T, Int]

  val contents: InternalStorage

  // _lookup = contents^-1
  type LookupTable = Map[Int, T]
  protected lazy val _lookup: LookupTable = contents map { _.swap }

  /**
   * Return all the references contained in this registry
   * @return a Set of Integer referencing the contents of this
   */
  def references: Set[Int] = contents.map { case (t,i) => i }.toSet

  /**
   * Return all the values contained in this registry
   * @return a set of values
   */
  def values: Set[T] = contents.map { case (t,i) => t }.toSet

  /**
   * As registries are immutable, the associated size is a val
   */
  val size: Int  = contents.size

  /**
   * Access to the i_th element stored in this registry.
   *
   * Remark: This operation implies a time-consuming operation (O(|contents|) when
   * called the first time.
   *
   * @param i the index (must exists)
   * @return The instance of T located at index i in this registry
   */
  def apply(i: Int): T  = _lookup(i)

  /**
   * Access to the index of an element stored in the registry
   * @param t the element to look for
   * @return Some(idx) if t is located at the index idx in the registry, None elsewhere.
   */
  def apply(t: T): Option[Int] = contents.get(t)

  /**
   * Add an element to the registry in a functional way (no side effect, return a new InternalStorage), at the end.
   * @param t the element to add
   * @return a new map that contains the new element indexed at this.size.
   */
  protected def addToContents(t: T): InternalStorage = this.contents.get(t) match {
    case None => this.contents + (t -> this.size)
    case Some(_) => this.contents
  }

  /**
   * Append the content of another InternalStorage to the one contained in this.contents.
   *
   * Remark: this operator is not COMMUTATIVE !! It change the index of the points contained in its right parameter
   *
   * @param other
   * @return
   */
  protected def appendToContents(other: InternalStorage): InternalStorage = {
    (this.contents /: other) { case (acc, (p,_)) =>
      acc.get(p) match {
        case None    => acc + (p -> acc.size)
        case Some(_) => acc
      }
    }
  }
}

/**
 * A VertexRegistry store all the points used by a given mesh
 * @param contents A map binding points to indexes, default is the empty map
 */
case class VertexRegistry(override val contents: Map[Point, Int] = Map()) extends Registry[Point] {
  def +(p: Point) = this.copy(addToContents(p))
  def +(that: VertexRegistry) = this.copy(appendToContents(that.contents))
}

case class FaceRegistry(override val contents: Map[Face, Int]= Map())extends Registry[Face] {
  def +(t: Face) = this.copy(addToContents(t))
  def +(r: FaceRegistry) = this.copy(appendToContents(r.contents))

  /**
   * Look for a given face, based on its center (a vertex reference)
   * @param center the vertex reference used as index
   * @return None if no faces matched, Some(f) where f is the reference of the matched face elsewhere
   */
  def lookFor(center: Int): Option[Int] = this.contents.par.find { case (f,i) => f.center == center  } match {
    case None => None
    case Some((f,i)) => Some(i)
  }

  /**
   * Update the face located at a given index r, replacing it by a new one (f)
   * @param r the reference to be updated
   * @param f the new face
   * @return a new FaceRegistry, taking into account the update
   */
  def update(r: Int, f: Face): FaceRegistry = this.copy(contents = contents - this(r) + (f -> r))

}

case class EdgeRegistry(override val contents: Map[Edge, Int]= Map()) extends Registry[Edge] {
  def +(t: Edge) = this.copy(addToContents(t))
  def +(r: EdgeRegistry): EdgeRegistry = this.copy(appendToContents(r.contents))
}



