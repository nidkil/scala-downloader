package com.nidkil.downloader.io

import java.io.File
import java.net.URL
import org.scalatest.FunSpec
import org.scalatest.Matchers
import com.nidkil.downloader.datatypes.Chunk
import com.nidkil.downloader.utils.Checksum
import com.nidkil.downloader.utils.Timer
import com.nidkil.downloader.utils.UrlUtils
import org.scalatest.Tag

class DownloadProviderTest extends FunSpec with Matchers {

  val timer = new Timer()
  lazy val workDir = new File(curDir, "download")

  def curDir = new java.io.File(".").getCanonicalPath

  describe("A DownloadProvider") {
    it("should throw an exception when the protocol is not HTTP", Tag("unit")) {
      intercept[IllegalArgumentException] {
        val provider = new DownloadProvider()
        try {
          val url = new URL("https://download.thinkbroadband.com/5MB.zip")
          val file = new File(workDir, UrlUtils.extractFilename(url))
          val chunk = new Chunk(1, url, file, 0, 0)
          provider.download(chunk)
        } finally {
          provider.close
        }
      }
    }
    it("should throw an exception when the URL does not exist") {
      intercept[IllegalArgumentException] {
        val provider = new DownloadProvider()
        try {
          val url = new URL("http://download.thinkbroadband.com/5MB.zip_does_not_exist")
          val file = new File(workDir, UrlUtils.extractFilename(url))
          val chunk = new Chunk(1, url, file, 0, 0)
          provider.download(chunk)
        } finally {
          provider.close
        }
      }
    }
    it("should throw an exception when the offset is negative or the length is zero") {
      intercept[IllegalArgumentException] {
        val provider = new DownloadProvider()
        try {
          val url = new URL("http://download.thinkbroadband.com/5MB.zip_does_not_exist")
          val file = new File(workDir, UrlUtils.extractFilename(url))
          val chunk = new Chunk(1, url, file, -1, 1000)
          provider.download(chunk)
        } finally {
          provider.close
        }
      }
      intercept[IllegalArgumentException] {
        val provider = new DownloadProvider()
        try {
          val url = new URL("http://download.thinkbroadband.com/5MB.zip_does_not_exist")
          val file = new File(workDir, UrlUtils.extractFilename(url))
          val chunk = new Chunk(1, url, file, 0, 0)
          provider.download(chunk)
        } finally {
          provider.close
        }
      }
    }
    it("should download the file") {
      timer.start

      val provider = new DownloadProvider()

      try {
        val url = new URL("http://apache.proserve.nl/tomcat/tomcat-7/v7.0.56/bin/apache-tomcat-7.0.56.zip")
        val file = new File(workDir, UrlUtils.extractFilename(url))
        val rfi = provider.remoteFileInfo(url)
        val chunk = new Chunk(1, url, file, 0, rfi.fileSize.toInt)
        provider.download(chunk)

        info("the content length must match the file length")
        assert(file.length == rfi.fileSize)

        info("the MD5 checksum of the downloaded file must match the MD5 checksum on the website")
        assert(Checksum.calculate(file) == "2bc8949a9c2ac44c5787b9ed4cfd3d0d")

        file.delete
      } finally {
        provider.close
      }

      timer.stop

      info(s"download time=${timer.execTime()}")
    }
  }

}