Dialog for Android
==================

This library for Android provides a Dialog including material sheet dialogs.
**Minimum SDK** is **12** (Android 3.1.x Honeycomb).

Please note that this version is not production ready!

Features included:
- Gravity options: BOTTOM (as material sheet dialog), CENTER (as standard dialog) and FULLSCREEN.
- Custom header and footer views
- Sticky footer (footer is always visible)
- Supports different content views.
  Default implementations are ```ListViewHolder```, ```RecyclerViewHolder``` and ```ViewHolder``` (for custom layouts)
- Collapsible bottom sheet dialog like the default share dialog of Android 5.0 Lollipop.

## Download

gradle:

```groovy
compile 'ch.temparus.android:dialog:1.2.0-SNAPSHOT'
```

Maven:
```xml
<dependency>
  <groupId>ch.temparus.android</groupId>
  <artifactId>dialog</artifactId>
  <version>1.2.0-SNAPSHOT</version>
  <type>aar</type>
</dependency>
```

## Usage

You can create a new Dialog with ```Dialog.Builder```. 

Example:
```java
Dialog dialog = new Dialog.Builder()
                    .setContentHolder(new ListViewHolder(adapter))
                    .setGravity(Dialog.Gravity.BOTTOM)
                    .setHeader(R.layout.header)
                    .setFooter(R.layout.footer)
                    .setOnCancelListener(onCancelListener)
                    .setOnItemClickListener(onItemClickListener)
                    .create();
```

You can display the dialog with ```dialog.show()```.
If this wasn't enough, you can grab the ```sample``` from the sample directory of this repository.

## Change Log

See [here](https://github.com/sandrolutz/Android-Dialog/blob/develop/CHANGELOG.md).

## License

    Copyright 2015 Sandro Lutz

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
