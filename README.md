Official source:

https://android.googlesource.com/platform/frameworks/opt/datetimepicker/
(Android 4.3+)

#DateTimePicker 

DateTimePicker is a library which contains the beautiful DatePicker and TimePicker that can be seen in the new Google Agenda app.

**This picker is available for 2.1+**

You have a recurrence picker in the same style [here](https://github.com/Shusshu/Android-RecurrencePicker).

## WARNING

* Accessibility is missing for DatePicker on all devices and Below ICS devices for TimePicker.

## Description

This library reproduces as much as possible the original picker contained in the new Google Agenda app.

![Example Image][1]

Or browse the [source code of the sample application][3] for a complete example of use.

## Including in your project

Last version is 0.0.7

Just add the following in your build.gradle

```groovy
repositories{
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    compile 'com.github.lachlanm:datetimepicker:0.0.7'
}
```

## Usage

1. **Basic**

  Date Picker
  ```java
  DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new OnDateSetListener() {...}, year, month, day);
  ```

  Time Picker
  ```java
  TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {...}, hourOfDay, minute, is24HourMode);
  ```
  For more info look at the source code of the provided sample [here][4]

2. **Change Dialog Primary Color**

  Colors will be based off AppCompat colorPrimary and colorPrimaryDark. You must use a theme based on Theme.AppCompat.

3. **Set Min/Max Date**
  ```java
  datePickerDialog.setDateConstaints(new SimpleMonthAdapter.CalendarDay(2015, 4, 17), new SimpleMonthAdapter.CalendarDay(2015, 4, 20));
  ```

## Acknowledgements

* Thanks to Google for this beautiful picker

## License

    Copyright 2013 Flavien Laurent (DatePicker) edisonw (TimePicker)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [1]: https://raw.githubusercontent.com/lachlanm/datetimepicker/master/graphics/img1.png
 [3]: https://github.com/lachlanm/datetimepicker/tree/master/datetimepicker-sample
 [4]: https://github.com/lachlanm/datetimepicker/blob/master/datetimepicker-sample/src/com/fourmob/datetimepicker/sample/MainActivity.java
 [5]: http://nineoldandroids.com/
