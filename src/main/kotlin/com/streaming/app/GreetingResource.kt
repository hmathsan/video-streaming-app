package com.streaming.app

import com.streaming.app.constants.StreamingConstants
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestHeader
import org.jboss.resteasy.reactive.RestPath
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

@Path("/video")
class GreetingResource {

    @GET
    @Path("/{videoId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM, "video/mp4")
    fun streamVideo(
        @RestPath videoId: String?,
        @RestHeader("Range") range: String?
    ) : Response {

        var path = Paths.get("C:\\Users\\mathe\\Videos\\Test Streaming\\compressed_source1.mp4")

        var fileLength = path.toFile().length()

        var ranges = range?.replace("bytes=", "")?.split("-")
        var rangeStart = ranges?.get(0)?.toLong()!!
        var rangeEnd: Long = rangeStart + StreamingConstants.MAX_CHUNK_SIZE.value - 1

        if (rangeEnd >= fileLength) {
            rangeEnd = fileLength - 1
        }

        println("range start : $rangeStart")
        println("range end : $rangeEnd")
        var inputStream: InputStream

        try {
            inputStream = Files.newInputStream(path)
            inputStream.skip(rangeStart)
            var contentLength = rangeEnd - rangeStart + 1

            var data = ByteArray(contentLength.toInt())
            var read = inputStream.read(data, 0, data.size)
            println("read(number of bytes) : $read")

            var response = Response.ok(data)
            response.type(MediaType.APPLICATION_OCTET_STREAM)
            response.header(HttpHeaders.CONTENT_LENGTH, contentLength)
            response.header("Content-Range", "bytes $rangeStart-$rangeEnd/$fileLength")
            response.status(Response.Status.PARTIAL_CONTENT)

            return response.build()
        } catch (e: Exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build()
        }
    }
}