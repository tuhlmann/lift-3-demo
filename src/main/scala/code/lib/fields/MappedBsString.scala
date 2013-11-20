package code.lib.fields

import net.liftweb.mapper.MappedString
import net.liftweb.mapper.Mapper

abstract class MappedBsString[T<:Mapper[T]](_fieldOwner: T, _maxLen: Int) extends MappedString(_fieldOwner, _maxLen) {



}