package eu.ace_design.island.stdlib

import eu.ace_design.island.map.resources.Conditions.Condition
import eu.ace_design.island.map.resources.Soils.Soil
import eu.ace_design.island.map.resources.{Soils, Conditions, ExploitationDistribution}
import eu.ace_design.island.stdlib.ExistingBiomes._

import scala.util.Random

/**
 * The standard distribution considers a 20/60/20 distribution of both soil types and exploitation conditions. It does
 * not take into account the biome in the assignment, purely random distribution.
 */
object StandardDistribution extends ExploitationDistribution {

  override val supported: Set[Biome] = ExistingBiomes.values

  override protected def condition(b: Biome, random: Random): Condition = random.nextDouble() match {
    case x if x < 0.2 => Conditions.EASY
    case x if x < 0.8 => Conditions.FAIR
    case _            => Conditions.HARSH
  }

  override protected def soil(b: Biome, random: Random): Soil = random.nextDouble() match {
    case x if x < 0.2 => Soils.FERTILE
    case x if x < 0.8 => Soils.NORMAL
    case _            => Soils.POOR
  }

}
