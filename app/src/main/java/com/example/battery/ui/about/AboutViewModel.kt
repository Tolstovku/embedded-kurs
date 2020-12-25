package com.example.battery.ui.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Курсовая работа студентов ИТМО группы P3400 \n" +
                "Толстов Даниил\n" +
                "Вербовой Александр\n" +
                "Мальцев Андрей\n" +
                "Авраменко Антон\n" +
                "Давыдов Иван\n" +
                "Шишкин Никита\n" +
                "Рожнов Денис"
    }
    val text: LiveData<String> = _text
}