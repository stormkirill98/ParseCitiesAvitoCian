package saving

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Read city names from .csv file
 * File put into resources package
 * Raw format: {number},{name} (1, Москва)
 *
 * @param fileName
 * @return list of city names
 */
fun readCitiesCsv(fileName: String): List<String> {
    val fileText = getFile("$fileName.csv").readText()

    return fileText.lines().map { it.substringAfter(",").trim() }
}

/**
 * Read city names from .json file
 * File put into resources package
 * File format:
 * {
 *   cities: [
 *      {
 *        id: 'int',
 *        name: 'string' ,
 *        cian_url: 'string',
 *        cian_id: 'int',
 *        avito_url: 'string',
 *        avito_id: 'int'
 *      },
 *      ...
 *   ]
 *}
 * @param fileName
 * @return list of CityDto
 */
fun readCitiesJson(fileName: String): List<CityDto> {
    val fileText = getFile("$fileName.json").readText()

    try {
        val jsonArray = JSONObject(fileText).getJSONArray("cities")

        return jsonArray.map {
            val json = it as JSONObject

            val name = json.getString("name")
            val cianUrl = try { json.getString("cian_url") } catch (e: Exception) { "" }
            val cianId = try { json.getInt("cian_id") } catch (e: Exception) { 0 }
            val avitoUrl = try { json.getString("avito_url") } catch (e: Exception) { "" }
            val avitoId = try { json.getInt("avito_id") } catch (e: Exception) { 0 }

            return@map CityDto(name, avitoUrl, avitoId, cianUrl, cianId)
        }
    } catch (e: Exception) { e.printStackTrace(); return emptyList() }
}

/**
 * Write cities to .json file
 * File format:
 * {
 *   cities: [
 *      {
 *        name: 'string' ,
 *        cian_url: 'string',
 *        cian_id: 'int',
 *        avito_url: 'string',
 *        avito_id: 'int',
 *        districts: [
 *          {
 *            name: 'string',
 *            avito_id: 'int',
 *            cian_id: 'int'
 *          },
 *          ...
 *        ]
 *      },
 *      ...
 *   ]
 *} * File is in the resources folder
 *
 * @param fileName
 * @param jsonArray JSONArray of cities
 */
fun writeCities(fileName: String, jsonArray: JSONArray) {
    val file = getFile("$fileName.json", true)
    val jsonObject = JSONObject().put("cities", jsonArray)
    file.appendText(jsonObject.toString(2))
}

/**
 * Write cities with districts to .json file
 * Output file structure is json object with array of cities by key 'cities'
 * File is in the resources folder
 *
 * @param fileName
 * @param jsonArray JSONArray of cities
 */
fun writeCitiesWithDistricts(
    fileName: String,
    cities: List<CityDto>
) {
    val jsonArray = JSONArray(cities.map { CityDto::toJSON })
    val json = JSONObject().put("cities", jsonArray)
    val file = getFile("$fileName.json", true)
    file.appendText(json.toString(2))
}

private fun pathToResourceFolder() = System.getProperty("user.dir") + "\\src\\resources\\"

private fun getFile(name: String, empty: Boolean = false): File {
    val path = pathToResourceFolder() + name
    val file = File(path)

    if (file.exists() && empty) file.delete()
    file.createNewFile()

    return file
}