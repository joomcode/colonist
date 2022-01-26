package com.joom.colonist.plugin

import java.io.File

open class AndroidColonistExtension {
  var cacheable: Boolean = false
  var bootClasspath: List<File> = emptyList()
}
