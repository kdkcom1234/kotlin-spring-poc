import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

fun main() {
    val url = URL("https://spring-native-run-a4lgvrdoba-du.a.run.app/hello")

    val start = Date().time
    val connection = url.openConnection()
    BufferedReader(InputStreamReader(connection.getInputStream())).use { inp ->
        var line: String?
        while (inp.readLine().also { line = it } != null) {
            println(line)
        }
    }
    var end = Date().time

    println("duration : ${end - start}")
}