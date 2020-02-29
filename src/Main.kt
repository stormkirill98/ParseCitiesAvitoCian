import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URLEncoder
import java.nio.charset.Charset

fun main() {
    Logger.logNewRunning()
//    connectDB()
    val cities = readCities()

    for (city in cities) {
        println("Parse $city")
        Logger.currentCity = city
        checkCityName(city)
        /*try {
            val avitoCityId = getAvitoCityId(city)
            if (avitoCityId < 0) Logger.logWrongCityId(avitoCityId, "avito")

            val cianCityId = getCianCityId(city)
            if (cianCityId < 0) Logger.logWrongCityId(cianCityId, "cian")

            if (avitoCityId < 0 || cianCityId < 0) continue;

            val districts = getDistricts(avitoCityId, cianCityId)
            writeCityWithDistricts(city, districts)
            Thread.sleep(1000)
        } catch (e: JSONException) {
            Logger.logException(e)
        }*/

        Thread.sleep(1000)
    }
}

fun readCities(): List<String> {
    val file = {}.javaClass.getResource(
        "1,Москва\n2,Санкт-Петербург\n3,Новосибирск\n4,Екатеринбург\n5,Нижний Новгород\n6,Казань\n7,Челябинск\n8,Омск\n9,Самара\n10,Ростов-на-Дону\n11,Уфа\n12,Красноярск\n13,Воронеж\n14,Пермь\n15,Волгоград\n16,Краснодар\n17,Саратов\n18,Тюмень\n19,Тольятти\n20,Ижевск\n21,Барнаул\n22,Ульяновск\n23,Иркутск\n24,Хабаровск\n25,Ярославль\n26,Владивосток\n27,Махачкала\n28,Томск\n29,Оренбург\n30,Кемерово\n31,Новокузнецк\n32,Рязань\n33,Астрахань\n34,Набережные Челны\n35,Пенза\n36,Киров\n37,Липецк\n38,Чебоксары\n39,Балашиха\n40,Калининград\n41,Тула\n42,Курск\n43,Севастополь\n44,Сочи\n45,Ставрополь\n46,Улан-Удэ\n47,Тверь\n48,Магнитогорск\n49,Брянск\n50,Иваново\n51,Белгород\n52,Сургут\n53,Владимир\n54,Чита\n55,Нижний Тагил\n56,Архангельск\n57,Симферополь\n58,Калуга\n59,Смоленск\n60,Волжский\n61,Якутск\n62,Саранск\n63,Череповец\n64,Курган\n65,Вологда\n66,Орёл\n67,Грозный\n68,Владикавказ\n69,Подольск\n70,Тамбов\n71,Мурманск\n72,Петрозаводск\n73,Нижневартовск\n74,Стерлитамак\n75,Кострома\n76,Новороссийск\n77,Йошкар-Ола\n78,Химки\n79,Таганрог\n80,Сыктывкар\n81,Комсомольск-на-Амуре\n82,Нижнекамск\n83,Нальчик\n84,Шахты\n85,Дзержинск\n86,Братск\n87,Орск\n88,Благовещенск\n89,Энгельс\n90,Ангарск\n91,Великий Новгород\n92,Королёв\n93,Старый Оскол\n94,Мытищи\n95,Псков\n96,Люберцы\n97,Южно-Сахалинск\n98,Бийск\n99,Прокопьевск\n100,Армавир\n101,Балаково\n102,Абакан\n103,Рыбинск\n104,Северодвинск\n105,Петропавловск-Камчатский\n106,Норильск\n107,Уссурийск\n108,Волгодонск\n109,Красногорск\n110,Сызрань\n111,Новочеркасск\n112,Каменск-Уральский\n113,Златоуст\n114,Электросталь\n115,Альметьевск\n116,Миасс\n117,Керчь\n118,Салават\n119,Копейск\n120,Находка\n121,Пятигорск\n122,Хасавюрт\n123,Майкоп\n124,Рубцовск\n125,Березники\n126,Коломна\n127,Одинцово\n128,Ковров\n129,Домодедово\n130,Кисловодск\n131,Нефтекамск\n132,Нефтеюганск\n133,Батайск\n134,Новочебоксарск\n135,Дербент\n136,Серпухов\n137,Щёлково\n138,Каспийск\n139,Черкесск\n140,Новомосковск\n141,Первоуральск\n142,Раменское\n143,Назрань\n144,Кызыл\n145,Обнинск\n146,Орехово-Зуево\n147,Новый Уренгой\n148,Невинномысск\n149,Димитровград\n150,Октябрьский\n151,Долгопрудный\n152,Камышин\n153,Ессентуки\n154,Муром\n155,Жуковский\n156,Евпатория\n157,Новошахтинск\n158,Реутов\n159,Пушкино\n160,Артём\n161,Северск\n162,Ноябрьск\n163,Ачинск\n164,Арзамас\n165,Бердск\n166,Элиста\n167,Ногинск\n168,Елец\n169,Сергиев Посад\n170,Новокуйбышевск\n171,Железногорск"
    )
    val fileText = file.readText(Charset.forName("windows-1251"))

    return fileText.lines().map { it.substringAfter(",") }
}

fun getAvitoCityId(cityName: String): Int {
    val encodeCityName = URLEncoder.encode(cityName, Charsets.UTF_8.toString())
    val res = fetchData("https://www.avito.ru/web/1/slocations?limit=5&q=$encodeCityName")

    val locations = JSONObject(res).getJSONObject("result")?.getJSONArray("locations")

    val firstLocation = locations?.first() as JSONObject? ?: return -1
    val nameInJson = firstLocation.getJSONObject("names")?.getString("1") ?: return -2
    if (nameInJson != cityName) {
        Logger.logWrongName(cityName, nameInJson, "avito")
    }

    return firstLocation.getInt("id")
}

fun getCianCityId(cityName: String): Int {
    val encodeCityName = URLEncoder.encode(cityName, Charsets.UTF_8.toString())
    val res = fetchData("https://yaroslavl.cian.ru/cian-api/site/v1/search-regions-cities/?text=$encodeCityName")

    val locations = JSONObject(res).getJSONObject("data")?.getJSONArray("items")

    val firstLocation = locations?.first() as JSONObject? ?: return -1
    val nameInJson = firstLocation.getString("displayName") ?: return -2
    if (nameInJson != cityName) {
        Logger.logWrongName(cityName, nameInJson, "cian")
    }

    return firstLocation.getInt("id")
}

fun getDistricts(avitoCityId: Int, cianCityId: Int): List<DistrictDto> {
    var isMetro = false

    Thread.sleep(500)
    val resAvito = try {
        fetchData("https://www.avito.ru/web/1/locations/districts?locationId=$avitoCityId")
    } catch (e: FileNotFoundException) {
        isMetro = true
        fetchData("https://www.avito.ru/web/1/locations/metro?locationId=$avitoCityId")
    }
    val avitoDistricts = parseDistrictsJson(JSONArray(resAvito), "avito", isMetro)

    /*if (isMetro) {
       HeadlessBrowser.getMetroCian("asd")
   } else {*/
    val resCian = fetchData("https://yaroslavl.cian.ru/api/geo/get-districts-tree/?locationId=$cianCityId")
    val cianDistricts = parseDistrictsJson(JSONArray(resCian), "cian", isMetro)

    return combineDistricts(avitoDistricts, cianDistricts)
}

fun parseDistrictsJson(array: JSONArray, cite: String, isMetro: Boolean): ArrayList<DistrictDto> {
    val districts = arrayListOf<DistrictDto>()

    for (obj in array) {
        if (obj !is JSONObject) continue

        val id = obj.getInt("id")
        val name = obj.getString("name")

        val district = if (cite == "cian")
            DistrictDto(name, idCian = id, isMetro = isMetro)
        else DistrictDto(name, idAvito = id, isMetro = isMetro)

        districts.add(district)
    }

    return districts
}

fun combineDistricts(avitoDistricts: List<DistrictDto>, cianDistricts: List<DistrictDto>): List<DistrictDto> {
    if (avitoDistricts.size != cianDistricts.size) {
        Logger.logNotEqualsDistrictsSize(avitoDistricts.size, cianDistricts.size)
    }

    val size = if (avitoDistricts.size > cianDistricts.size)
        avitoDistricts.size
    else cianDistricts.size

    val newList = ArrayList<DistrictDto>(size)

    for (i in 0 until size) {
        if (i < cianDistricts.size) {
            val districtFromCian = cianDistricts[i]
            val districtFromAvito = getDistrictByName(avitoDistricts, districtFromCian.name)

            if (districtFromAvito == null) Logger.logNotFoundDistrict(districtFromCian.name, "avito")

            districtFromAvito?.let { districtFromCian.idAvito = districtFromAvito.idAvito }
            newList.add(districtFromCian)
            continue
        }

        if (i < avitoDistricts.size) {
            val districtFromAvito = avitoDistricts[i]
            val districtFromCian = getDistrictByName(cianDistricts, districtFromAvito.name)

            if (districtFromCian == null) Logger.logNotFoundDistrict(districtFromAvito.name, "cian")

            districtFromCian?.let { districtFromAvito.idCian = districtFromCian.idCian }
            newList.add(districtFromAvito)
            continue
        }
    }

    return newList
}

fun checkCityName(name: String): Boolean {
    var result = true
    val transliterateName = transliterateCyrillicToLatin(name)

    Thread.sleep(500)
    if (!checkUrl("https://www.avito.ru/$transliterateName")) {
        Logger.logWrongTransliterateCityName(transliterateName, "avito")
        result = false
    }

    if (!checkUrl("https://$transliterateName.cian.ru/")) {
        Logger.logWrongTransliterateCityName(transliterateName, "cian")
        result = false
    }

    return result
}

fun getDistrictByName(list: List<DistrictDto>, name: String): DistrictDto? {
    for (district in list) {
        if (district.name == name) return district
    }

    return null
}