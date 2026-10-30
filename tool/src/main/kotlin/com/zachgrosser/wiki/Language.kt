package com.zachgrosser.wiki

// Latin-alphabet languages only, since that's all Light's keyboard can type.
enum class Language(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val strings: UiStrings,
    // Local names for non-article pages ("Datei:", "Kategorie:"), so links to them aren't followed.
    val namespaces: Set<String> = emptySet(),
) {
    English("en", "English", "English", ENGLISH),
    Spanish(
        "es", "Español", "Spanish", SPANISH,
        setOf("especial", "archivo", "imagen", "categoría", "plantilla", "ayuda", "portal", "anexo", "usuario", "discusión"),
    ),
    French(
        "fr", "Français", "French", FRENCH,
        setOf("spécial", "fichier", "image", "catégorie", "modèle", "aide", "wikipédia", "portail", "utilisateur", "discussion", "projet"),
    ),
    German(
        "de", "Deutsch", "German", GERMAN,
        setOf("spezial", "datei", "bild", "kategorie", "vorlage", "hilfe", "portal", "benutzer", "diskussion"),
    ),
    Italian(
        "it", "Italiano", "Italian", ITALIAN,
        setOf("speciale", "file", "immagine", "categoria", "template", "aiuto", "portale", "utente", "discussione", "progetto"),
    ),
    Portuguese(
        "pt", "Português", "Portuguese", PORTUGUESE,
        setOf("especial", "ficheiro", "arquivo", "imagem", "categoria", "predefinição", "ajuda", "wikipédia", "portal", "usuário", "utilizador", "discussão"),
    ),
    Dutch(
        "nl", "Nederlands", "Dutch", DUTCH,
        setOf("speciaal", "bestand", "afbeelding", "categorie", "sjabloon", "help", "portaal", "gebruiker", "overleg"),
    ),
    Swedish(
        "sv", "Svenska", "Swedish", SWEDISH,
        setOf("special", "fil", "bild", "kategori", "mall", "hjälp", "portal", "användare", "diskussion"),
    ),
    ;

    val host: String get() = "$code.wikipedia.org"

    companion object {
        fun fromCode(code: String?): Language = entries.firstOrNull { it.code == code } ?: English
    }
}

// Wiki's own text. Credits and About's policies stay in English (see Attribution).
class UiStrings(
    val settings: String,
    val about: String,
    val aboutIntro: String,
    val language: String,
    val saved: String,
    val savedHint: String,
    val searchPlaceholder: String,
    val search: String,
    val back: String,
    val loading: String,
    val searching: String,
    val contents: String,
    val next: (sectionTitle: String) -> String,
    val savedForOffline: String,
    val removedFromSaved: String,
    val saveAction: String,
    val removeAction: String,
    val noArticle: (title: String) -> String,
    val noResults: (query: String) -> String,
    val busy: String,
    val offline: String,
    val savedStillReadable: String,
)

private val ENGLISH = UiStrings(
    settings = "Settings",
    about = "About",
    aboutIntro = "Wiki lets you search and read Wikipedia on your Light Phone. Read an article a " +
        "section at a time, follow its links, and star it to read it offline.",
    language = "Language",
    saved = "Saved",
    savedHint = "Tap the star on an article to keep it here for reading offline.",
    searchPlaceholder = "Search Wikipedia",
    search = "Search",
    back = "Back",
    loading = "Loading…",
    searching = "Searching…",
    contents = "Contents",
    next = { "Next: $it" },
    savedForOffline = "Saved for reading offline.",
    removedFromSaved = "Removed from saved.",
    saveAction = "Save for reading offline",
    removeAction = "Remove from saved",
    noArticle = { "Wikipedia doesn't have an article called “$it”." },
    noResults = { "No articles found for “$it”." },
    busy = "Wikipedia is busy right now. Please try again in a minute.",
    offline = "Couldn't reach Wikipedia. Check your connection and try again.",
    savedStillReadable = "Saved articles can still be read offline.",
)

private val SPANISH = UiStrings(
    settings = "Ajustes",
    about = "Acerca de",
    aboutIntro = "Wiki te permite buscar y leer Wikipedia en tu Light Phone. Lee un artículo " +
        "sección por sección, sigue sus enlaces y márcalo con la estrella para leerlo sin conexión.",
    language = "Idioma",
    saved = "Guardados",
    savedHint = "Toca la estrella de un artículo para guardarlo aquí y leerlo sin conexión.",
    searchPlaceholder = "Buscar en Wikipedia",
    search = "Buscar",
    back = "Atrás",
    loading = "Cargando…",
    searching = "Buscando…",
    contents = "Contenido",
    next = { "Siguiente: $it" },
    savedForOffline = "Guardado para leer sin conexión.",
    removedFromSaved = "Quitado de guardados.",
    saveAction = "Guardar para leer sin conexión",
    removeAction = "Quitar de guardados",
    noArticle = { "Wikipedia no tiene ningún artículo llamado «$it»." },
    noResults = { "No se encontraron artículos para «$it»." },
    busy = "Wikipedia está muy ocupada ahora mismo. Inténtalo de nuevo en un minuto.",
    offline = "No se pudo conectar con Wikipedia. Comprueba tu conexión e inténtalo de nuevo.",
    savedStillReadable = "Los artículos guardados se pueden seguir leyendo sin conexión.",
)

// French takes a no-break space before ":" and inside « ».
private val FRENCH = UiStrings(
    settings = "Paramètres",
    about = "À propos",
    aboutIntro = "Wiki vous permet de chercher et de lire Wikipédia sur votre Light Phone. Lisez un " +
        "article section par section, suivez ses liens et ajoutez-lui une étoile pour le lire hors connexion.",
    language = "Langue",
    saved = "Enregistrés",
    savedHint = "Touchez l'étoile d'un article pour le garder ici et le lire hors connexion.",
    searchPlaceholder = "Rechercher sur Wikipédia",
    search = "Rechercher",
    back = "Retour",
    loading = "Chargement…",
    searching = "Recherche…",
    contents = "Sommaire",
    next = { "Suivant : $it" },
    savedForOffline = "Enregistré pour la lecture hors connexion.",
    removedFromSaved = "Retiré des enregistrés.",
    saveAction = "Enregistrer pour lire hors connexion",
    removeAction = "Retirer des enregistrés",
    noArticle = { "Wikipédia n'a pas d'article intitulé « $it »." },
    noResults = { "Aucun article trouvé pour « $it »." },
    busy = "Wikipédia est très sollicitée en ce moment. Réessayez dans une minute.",
    offline = "Impossible de joindre Wikipédia. Vérifiez votre connexion et réessayez.",
    savedStillReadable = "Les articles enregistrés restent lisibles hors connexion.",
)

private val GERMAN = UiStrings(
    settings = "Einstellungen",
    about = "Info",
    aboutIntro = "Mit Wiki kannst du Wikipedia auf deinem Light Phone durchsuchen und lesen. Lies einen " +
        "Artikel Abschnitt für Abschnitt, folge seinen Links und markiere ihn mit dem Stern, um ihn offline zu lesen.",
    language = "Sprache",
    saved = "Gespeichert",
    savedHint = "Tippe auf den Stern eines Artikels, um ihn hier zum Offline-Lesen zu behalten.",
    searchPlaceholder = "Wikipedia durchsuchen",
    search = "Suchen",
    back = "Zurück",
    loading = "Wird geladen…",
    searching = "Suche läuft…",
    contents = "Inhalt",
    next = { "Weiter: $it" },
    savedForOffline = "Zum Offline-Lesen gespeichert.",
    removedFromSaved = "Aus „Gespeichert“ entfernt.",
    saveAction = "Zum Offline-Lesen speichern",
    removeAction = "Aus „Gespeichert“ entfernen",
    noArticle = { "Wikipedia hat keinen Artikel mit dem Titel „$it“." },
    noResults = { "Keine Artikel zu „$it“ gefunden." },
    busy = "Wikipedia ist gerade ausgelastet. Bitte versuche es in einer Minute erneut.",
    offline = "Wikipedia ist nicht erreichbar. Prüfe deine Verbindung und versuche es erneut.",
    savedStillReadable = "Gespeicherte Artikel kannst du weiterhin offline lesen.",
)

private val ITALIAN = UiStrings(
    settings = "Impostazioni",
    about = "Informazioni",
    aboutIntro = "Wiki ti permette di cercare e leggere Wikipedia sul tuo Light Phone. Leggi un " +
        "articolo una sezione alla volta, segui i suoi link e aggiungi una stella per leggerlo offline.",
    language = "Lingua",
    saved = "Salvati",
    savedHint = "Tocca la stella di un articolo per tenerlo qui e leggerlo offline.",
    searchPlaceholder = "Cerca su Wikipedia",
    search = "Cerca",
    back = "Indietro",
    loading = "Caricamento…",
    searching = "Ricerca in corso…",
    contents = "Indice",
    next = { "Avanti: $it" },
    savedForOffline = "Salvato per la lettura offline.",
    removedFromSaved = "Rimosso dai salvati.",
    saveAction = "Salva per leggere offline",
    removeAction = "Rimuovi dai salvati",
    noArticle = { "Wikipedia non ha un articolo intitolato «$it»." },
    noResults = { "Nessun articolo trovato per «$it»." },
    busy = "Wikipedia è sovraccarica in questo momento. Riprova tra un minuto.",
    offline = "Impossibile raggiungere Wikipedia. Controlla la connessione e riprova.",
    savedStillReadable = "Gli articoli salvati si possono comunque leggere offline.",
)

// Brazilian Portuguese.
private val PORTUGUESE = UiStrings(
    settings = "Configurações",
    about = "Sobre",
    aboutIntro = "Com o Wiki, você pode pesquisar e ler a Wikipédia no seu Light Phone. Leia um artigo " +
        "uma seção de cada vez, siga os links e marque-o com a estrela para ler offline.",
    language = "Idioma",
    saved = "Salvos",
    savedHint = "Toque na estrela de um artigo para guardá-lo aqui e ler offline.",
    searchPlaceholder = "Pesquisar na Wikipédia",
    search = "Pesquisar",
    back = "Voltar",
    loading = "Carregando…",
    searching = "Pesquisando…",
    contents = "Conteúdo",
    next = { "Próxima: $it" },
    savedForOffline = "Salvo para leitura offline.",
    removedFromSaved = "Removido dos salvos.",
    saveAction = "Salvar para ler offline",
    removeAction = "Remover dos salvos",
    noArticle = { "A Wikipédia não tem um artigo chamado “$it”." },
    noResults = { "Nenhum artigo encontrado para “$it”." },
    busy = "A Wikipédia está sobrecarregada no momento. Tente novamente em um minuto.",
    offline = "Não foi possível acessar a Wikipédia. Verifique sua conexão e tente novamente.",
    savedStillReadable = "Os artigos salvos ainda podem ser lidos offline.",
)

private val DUTCH = UiStrings(
    settings = "Instellingen",
    about = "Info",
    aboutIntro = "Met Wiki kun je Wikipedia doorzoeken en lezen op je Light Phone. Lees een artikel " +
        "per sectie, volg de links en geef het een ster om het offline te lezen.",
    language = "Taal",
    saved = "Bewaard",
    savedHint = "Tik op de ster bij een artikel om het hier te bewaren voor offline lezen.",
    searchPlaceholder = "Zoeken in Wikipedia",
    search = "Zoeken",
    back = "Terug",
    loading = "Laden…",
    searching = "Zoeken…",
    contents = "Inhoud",
    next = { "Volgende: $it" },
    savedForOffline = "Bewaard om offline te lezen.",
    removedFromSaved = "Verwijderd uit bewaard.",
    saveAction = "Bewaren om offline te lezen",
    removeAction = "Verwijderen uit bewaard",
    noArticle = { "Wikipedia heeft geen artikel met de naam ‘$it’." },
    noResults = { "Geen artikelen gevonden voor ‘$it’." },
    busy = "Wikipedia is nu erg druk. Probeer het over een minuut opnieuw.",
    offline = "Kan Wikipedia niet bereiken. Controleer je verbinding en probeer het opnieuw.",
    savedStillReadable = "Bewaarde artikelen kun je nog steeds offline lezen.",
)

private val SWEDISH = UiStrings(
    settings = "Inställningar",
    about = "Om Wiki",
    aboutIntro = "Med Wiki kan du söka och läsa Wikipedia på din Light Phone. Läs en artikel ett " +
        "avsnitt i taget, följ länkarna och stjärnmärk den för att läsa den offline.",
    language = "Språk",
    saved = "Sparade",
    savedHint = "Tryck på stjärnan i en artikel för att spara den här och läsa den offline.",
    searchPlaceholder = "Sök på Wikipedia",
    search = "Sök",
    back = "Tillbaka",
    loading = "Läser in…",
    searching = "Söker…",
    contents = "Innehåll",
    next = { "Nästa: $it" },
    savedForOffline = "Sparad för läsning offline.",
    removedFromSaved = "Borttagen från sparade.",
    saveAction = "Spara för läsning offline",
    removeAction = "Ta bort från sparade",
    noArticle = { "Wikipedia har ingen artikel som heter ”$it”." },
    noResults = { "Inga artiklar hittades för ”$it”." },
    busy = "Wikipedia är hårt belastat just nu. Försök igen om en minut.",
    offline = "Det gick inte att nå Wikipedia. Kontrollera anslutningen och försök igen.",
    savedStillReadable = "Sparade artiklar kan fortfarande läsas offline.",
)
