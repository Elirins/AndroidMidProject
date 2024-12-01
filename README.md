实验成果展示
====
        完成功能如下：
        NoteList界面中笔记条目增加时间戳显示
        添加笔记查询功能（根据标题查询）
        根据笔记标题或修改时间排序（升序）
        更换主界面的颜色
1. NoteList界面中笔记条目增加时间戳显示
----
图片效果展示：<br> 
![image](https://github.com/Elirins/AndroidMidProject/blob/master/app/src/main/res/drawable/readme_re01.png)<br> 
创建时能显示创建时间，后续修改时会显示修改时的时间。<br> 
第一步，在noteslist_item.xml 中添加时间戳的布局，并整体改为LinearLayout布局。<br> 
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="5dp">
    
    <!-- Title布局代码，沿用原代码，省略-->
    <!-- Timestamp TextView -->
    <TextView
        android:id="@+id/timestamp"
        android:layout_width="0dp"
        android:layout_weight="1"
        android:layout_height="?android:attr/listPreferredItemHeight"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:gravity="center_vertical"
        android:singleLine="true" />
        
</LinearLayout>
```
第二步，在NotesList.java中的onCreate方法中，给dataColumns和viewIDs增加数据条目：<br>
```Java
String[] dataColumns = {
    NotePad.Notes.COLUMN_NAME_TITLE, // 原有的标题
    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE // 时间戳
} ;
int[] viewIDs = {
    android.R.id.text1 , // 原有的标题
    R.id.timestamp // 时间戳
};
```
第三步，在SimpleCursorAdapter中，增加生成时间，显示时间的方法：<br>
```Java
@Override
public void bindView(View view, Context context, Cursor cursor) {
    super.bindView(view, context, cursor);
    // 获取时间戳字段并显示在 timestamp 上
    String timestamp = cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
    // 确保时间戳不为空
        if (timestamp != null && !timestamp.isEmpty()) {
            TextView timestampView = (TextView)view.findViewById(R.id.timestamp);
            timestampView.setText(formatTimestamp(timestamp));
        }
    }
// 格式化时间戳为可读的日期
private String formatTimestamp(String timestamp) {
    // 格式化时间戳
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    try {
        // 如果时间戳是一个长整型（毫秒）
        long timeInMillis = Long.parseLong(timestamp);
        return sdf.format(new Date(timeInMillis));
    } catch (NumberFormatException e) {
        e.printStackTrace();
        return "";
    }
}
```
2.添加笔记查询功能（根据标题查询）
----
图片效果展示：<br>
![image](https://github.com/Elirins/AndroidMidProject/blob/master/app/src/main/res/drawable/readme_re02_01.png)<br>
![image](https://github.com/Elirins/AndroidMidProject/blob/master/app/src/main/res/drawable/readme_re02_02.png)<br>
第一步，创建一个note_search布局文件：<br>
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingLeft="6dip"
    android:paddingRight="6dip"
    android:paddingBottom="3dip">

    <EditText android:id="@+id/search_input"
        android:maxLines="1"
        android:layout_marginTop="2dp"
        android:layout_marginBottom="15dp"
        android:layout_width="wrap_content"
        android:ems="25"
        android:layout_height="wrap_content"
        android:autoText="true"
        android:capitalize="sentences"
        android:scrollHorizontally="true" />

    <Button android:id="@+id/search_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="right"
        android:text="@string/search_button" />
</LinearLayout>
```
第二步，在NotesList类的onOptionItemSelected方法中的Switch分支语句中，增加选项菜单search的case：<br>
```Java
case R.id.menu_search:
    startActivityForResult(new Intent(this, Search.class), REQUEST_SEARCH);
    return true;
```
第三步，创建Search类，实现返回查询内容：<br>
```Java
package com.example.android.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Search extends Activity {
    private EditText searchInput;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);

        searchInput = findViewById(R.id.search_input);
        confirmButton = findViewById(R.id.search_button);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchInput.getText().toString().trim();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("query", query);  // 将查询内容传递回去
                setResult(RESULT_OK, resultIntent);  // 返回到 NotesList
                finish();  // 关闭当前 Activity
            }
        });
    }
}
```
第四步，在AndroidManifest中注册Search的activity：<br>
```xml
<activity android:name="Search"
    android:label="@string/Search_name"
    android:theme="@android:style/Theme.Holo.Dialog">
    <intent-filter>
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```
第五步，在NotesList类中添加方法，完成查询，筛选，排序，显示等逻辑：<br>
```Java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_SEARCH && resultCode == RESULT_OK) {
        String query = data.getStringExtra("query");
        // 如果有搜索内容，进行过滤；否则，显示所有内容
        if (query != null && !query.isEmpty()) {
            filterNotes(query);
        } else {
            // 如果没有输入搜索内容，显示所有笔记
            displayAllNotes();
        }
    }
}

// 根据搜索内容过滤笔记
private void filterNotes(String query) {
    Cursor cursor = managedQuery(
        getIntent().getData(),  // 使用默认内容 URI
        PROJECTION,  // 返回笔记 ID 和标题
        NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " LIKE ?",  // 添加条件
        new String[]{"%" + query + "%", "%" + query + "%"},  // 查询条件
        NotePad.Notes.DEFAULT_SORT_ORDER  // 使用默认排序
    );
    // 更新 ListView
    updateListView(cursor);
}
// 显示所有笔记
private void displayAllNotes() {
    Cursor cursor = managedQuery(
        getIntent().getData(),  // 使用默认内容 URI
        PROJECTION,  // 返回笔记 ID 和标题
        null,  // 不加任何条件
        null,  // 不需要额外的查询条件
        NotePad.Notes.DEFAULT_SORT_ORDER  // 使用默认排序
    );
    // 更新 ListView
    updateListView(cursor);
}
// 更新 ListView 的适配器
// 在 updateListView 中更新时间戳格式化
private void updateListView(Cursor cursor) {
    // 创建一个 SimpleDateFormat 来格式化时间戳
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    // 创建一个适配器
    SimpleCursorAdapter adapter = new SimpleCursorAdapter(
        this,
        R.layout.noteslist_item,
        cursor,
        new String[]{NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE},
        new int[]{android.R.id.text1, R.id.timestamp}) {
            @Override
            public void setViewText(TextView v, String value) {
                // 如果是时间戳字段，则将它格式化为日期
                if (v.getId() == R.id.timestamp) {
                    try {
                        long timestamp = Long.parseLong(value);
                        String formattedDate = dateFormat.format(new Date(timestamp));
                        v.setText(formattedDate);
                    } catch (NumberFormatException e) {
                        // 如果无法转换为长整型，直接显示原始值
                        v.setText(value);
                    }
                } else {
                    super.setViewText(v, value);
                }
            }
        };
    // 设置适配器
    setListAdapter(adapter);
}
```
3.根据笔记标题或修改时间排序（升序）
----
图片效果展示：<br>
![image](https://github.com/Elirins/AndroidMidProject/blob/master/app/src/main/res/drawable/readme_re03_01.png)<br>
时间升序排序效果：<br>
![image](https://github.com/Elirins/AndroidMidProject/blob/master/app/src/main/res/drawable/readme_re03_02.png)<br>
标题升序排序效果：<br>
![image](https://github.com/Elirins/AndroidMidProject/blob/master/app/src/main/res/drawable/readme_re03_03.png)<br>
由于每次修改，最新修改的笔记都会自动到最上方，故时间排序功能把修改时间早的笔记前置。<br>
第一步，在list_options_menu.xml中，添加排序选项的item：<br>
```xml
<item android:id="@+id/menu_sort_time"
        android:icon="@drawable/ic_menu_compose"
        android:title="@string/menu_sort_time"/>
<item android:id="@+id/menu_sort_title"
        android:icon="@drawable/ic_menu_compose"
        android:title="@string/menu_sort_titl
```
第二步，在NotesList类的onOptionItemSelected方法中的Switch分支语句中，增加选项的case：<br>
```Java
case R.id.menu_sort_time:
    // 点击 "按时间排序" 选项时，更新笔记列表
    sortNotesByTime();
    return true;
case R.id.menu_sort_title:
    // 点击 "按标题排序" 选项时，更新笔记列表
    sortNotesByTitle();
    return true;
```
第三步，在NotesList.java中添加方法，实现排序功能的逻辑：<br>
```Java
// 按修改时间排序
private void sortNotesByTime() {
    Cursor cursor = managedQuery(
        getIntent().getData(),
        PROJECTION,
        null, // 无条件
        null,
        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " ASC"  // 按时间升序排序
    );
    // 更新 ListView
    updateListView(cursor);
}
// 按标题排序
private void sortNotesByTitle() {
    Cursor cursor = managedQuery(
        getIntent().getData(),
        PROJECTION,
        null, // 无条件
        null,
        NotePad.Notes.COLUMN_NAME_TITLE + " ASC"  // 按标题升序排序
    );
    // 更新 ListView
    updateListView(cursor);
}
```
4.更换主界面的颜色
----
图片效果展示：<br>
![image](https://github.com/Elirins/AndroidMidProject/blob/master/app/src/main/res/drawable/readme_re04_01.png)<br>
![image](https://github.com/Elirins/AndroidMidProject/blob/master/app/src/main/res/drawable/readme_re04_02.png)<br>
第一步，在list_options_menu.xml中，添加换背景按钮的item：<br>
```xml
<item android:id="@+id/menu_color"
    android:icon="@drawable/ic_menu_color"
    android:title="@string/menu_color"
    android:showAsAction="always" />
```
第二步，在NotesList类的onOptionItemSelected方法中的Switch分支语句中，增加选项按钮的case：<br>
```Java
case R.id.menu_color:
    // 显示颜色选择对话框
    changeBackgroundColor();
    return true;
```
第三步，在NotesList.java中添加方法，实现换背景功能的逻辑：<br>
```Java
private void changeBackgroundColor() {
    // 预设颜色
    final String[] colors = {"White","Red", "Green", "Blue","Cyan"};
    // 使用 AlertDialog 让用户选择颜色
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("选择背景色")
            .setItems(colors, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 根据用户选择的颜色设置背景
                    String selectedColor = colors[which];
                    setBackgroundColor(selectedColor);
                }
            })
            .show();
}
private void setBackgroundColor(String colorName) {
    // 根据选中的颜色设置背景颜色
    int color = Color.WHITE; // 默认白色
    switch (colorName) {
        case "White":
            color = Color.WHITE;
            break;
        case "Red":
            color = Color.RED;
            break;
        case "Green":
            color = Color.GREEN;
            break;
        case "Blue":
            color = Color.BLUE;
            break;
        case "Cyan":
            color = Color.CYAN;
            break;
    }
    // 设置背景色
    getWindow().getDecorView().setBackgroundColor(color);
    // 保存颜色设置到 SharedPreferences 中
    SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString("background_color", colorName);
    editor.apply();
}
```
