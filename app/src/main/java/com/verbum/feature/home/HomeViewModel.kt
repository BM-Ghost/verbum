package com.verbum.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.feature.bible.domain.ContinueReadingState
import com.verbum.feature.bible.domain.GetContinueReadingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getContinueReading: GetContinueReadingUseCase,
) : ViewModel() {

    val continueReading = getContinueReading()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    data class VerseOfDay(
        val text: String,
        val reference: String,
    )

    data class TodaysMass(
        val title: String,
        val firstReading: String,
        val gospel: String,
    )

    val verseOfDay = VerseOfDay(
        text = "In the beginning was the Word, and the Word was with God, and the Word was God.",
        reference = "John 1:1",
    )

    val todaysMass = TodaysMass(
        title = "Wednesday of the 3rd Week of Easter",
        firstReading = "First Reading: Acts 8:1b-8",
        gospel = "Gospel: John 6:35-40",
    )

    fun seasonEmoji(season: LiturgicalSeason): String = when (season) {
        LiturgicalSeason.ADVENT -> "\uD83D\uDD6F\uFE0F"
        LiturgicalSeason.CHRISTMAS -> "\u2B50"
        LiturgicalSeason.LENT -> "\u271D\uFE0F"
        LiturgicalSeason.EASTER -> "\uD83C\uDF1E"
        LiturgicalSeason.PENTECOST -> "\uD83D\uDD25"
        LiturgicalSeason.ORDINARY_TIME -> "\uD83C\uDF3F"
    }

    fun seasonGreeting(season: LiturgicalSeason): String = when (season) {
        LiturgicalSeason.ADVENT -> "Prepare the way of the Lord"
        LiturgicalSeason.CHRISTMAS -> "The Word became flesh and dwelt among us"
        LiturgicalSeason.LENT -> "Return to the Lord with all your heart"
        LiturgicalSeason.EASTER -> "He is risen! Alleluia!"
        LiturgicalSeason.PENTECOST -> "Come, Holy Spirit, fill the hearts of your faithful"
        LiturgicalSeason.ORDINARY_TIME -> "Grow in the grace and knowledge of our Lord"
    }

    fun seasonPrayer(season: LiturgicalSeason): String = when (season) {
        LiturgicalSeason.ADVENT -> "Come, Lord Jesus. Fill our hearts with hope as we await your coming."
        LiturgicalSeason.CHRISTMAS -> "O God, who wonderfully created human dignity and still more wonderfully restored it, grant that we may share in the divinity of Christ."
        LiturgicalSeason.LENT -> "Lord, grant us the grace of true repentance. Help us turn our hearts back to You."
        LiturgicalSeason.EASTER -> "God of life, through the resurrection of your Son, you have filled us with joy. Help us be witnesses of your love."
        LiturgicalSeason.PENTECOST -> "Come, Holy Spirit, enkindle in us the fire of your love. Send forth your Spirit and renew the face of the earth."
        LiturgicalSeason.ORDINARY_TIME -> "Lord, guide our steps today. May we grow closer to You in all that we do."
    }
}
