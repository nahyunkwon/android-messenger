"Android Messanger with profanity filtering & profanity usage statistics"
=============

삐- 비속어 방지톡
------------

>삐- 비속어 방지톡은 메신저 사용자가 보내는 비속어를 필터링하고, 
얼마나 비속어를 사용했는지 통계로 보여 주어 사용자에게 비속어 사용에 대한 경각심을 주고, 
나아가 기존 메신저의 폐쇄적인 특성을 악용한 사이버 불링 등의 범죄를 방지하고자 하는 안드로이드 메신저이다.



# 1. Profanity Filtering(비속어 필터링)

## 1.1. 사용자 메시지 받기


```java
mSendButton = (Button) findViewById(R.id.sendButton);
         mSendButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String filteredText = getCensoredText(mMessageEditText.getText().toString());
                 FriendlyMessage friendlyMessage = new
                         FriendlyMessage(filteredText,
                         mUsername,
                         mPhotoUrl,
                         null /* no image */);
                 mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                         .push().setValue(friendlyMessage);
                 mMessageEditText.setText("");
             }
         });
```

         
비속어 필터링은 MainActivity의 사용자에게 메시지를 받는 부분에서 이루어진다.
사용자가 send 버튼을 누르면 mSendButton의 OnClick() 메소드가 실행되고, mMessageEditText를 통해 받아온
사용자의 메시지를 getCensoredText() 메소드에 전달하여 비속어가 필터링된 filteredText를 얻는다.
filteredText는 FriendlyMessage 형태로 파이어베이스 데이터베이스로 전송된다.
         
         
## 1.2. 비속어 필터링 메소드 호출

```java
String filteredText = getCensoredText(mMessageEditText.getText().toString());
```

사용자의 메시지는 getCensoredText() 메소드를 통해 비속어 탐지 후 비속어가 있으면 필터링된다.


## 1.3. 비속어 데이터베이스 load

```java
loadBadWords()
```

getCensoredText()는 loanBadWords() 메소드를 호출한다. 이 메소드는 사용자 메시지에서 비속어를 필터링하기 쉽도록 비속어 데이터를 정리하는 역할을 한다.

```java
InputStreamReader is = new InputStreamReader(getAssets().open("Word_Filter.csv")); //get bad word database from assets folder
```

loanBadWords() 메소드는 app/src/main/assets 폴더의 비속어 데이터를 불러온다.
비속어 데이터는 csv 파일 형식으로 저장하여 쉽게 데이터를 추가하고 삭제할 수 있다.

```java
// Make sure there are no capital letters in the spreadsheet
allBadWords.put(word.replaceAll(" ", "").toLowerCase(), ignore_in_combination_with_words);
```
                     
모든 비속어는 불필요한 공백을 제거한 후 allBadWords 해시맵에 저장된다.              


## 1.4. 비속어 필터링

```java
// remove leetspeak
modifiedInput = modifiedInput.replaceAll("1", "i").replaceAll("!", "i").replaceAll("3", "e").replaceAll("4", "a")
    .replaceAll("@", "a").replaceAll("5", "s").replaceAll("7", "t").replaceAll("0", "o").replaceAll("9", "g");
```

getCensoredText()에서는 먼저 input String에서 leetspeak을 제거한다.(영어 비속어의 경우)

```java
// iterate over each letter in the word
for (int start = 0; start < modifiedInput.length(); start++) {
    // from each letter, keep going to find bad words until either the end of
    // the sentence is reached, or the max word length is reached.
    for (int offset = 1; offset < (modifiedInput.length() + 1 - start) && offset < largestWordLength; offset++) {
        String wordToCheck = modifiedInput.substring(start, start + offset);
        if (allBadWords.containsKey(wordToCheck)) {
        String[] ignoreCheck = allBadWords.get(wordToCheck);
        boolean ignore = false;
        for (int stringIndex = 0; stringIndex < ignoreCheck.length; stringIndex++) {
            if (modifiedInput.contains(ignoreCheck[stringIndex])) {
                ignore = true;
                break;
            }
        }
        if (!ignore) {
            badWordsFound.add(wordToCheck);
        }
        }
    }
}
```
         
이후 input String의 단어를 반복하며 allBadWords의 비속어를 갖고 있는지 검사하여 비속어를 찾는다.
여기서 largestWordLength는 loanBadWords() 메소드에서 계산된, 비속어 데이터를 정리할 때 가장 긴 비속어의 길이를 의미한다.
         
         
```java
// replace each bad word character with *
String inputToReturn = input;
    for (String swearWord : badWordsFound) {
    char[] charsStars = new char[swearWord.length()];
    Arrays.fill(charsStars, '*');
    final String stars = new String(charsStars);
    // The "(?i)" is to make the replacement case insensitive.
    inputToReturn = inputToReturn.replaceAll("(?i)" + swearWord, stars);
    }
 ```
         
input String에 비속어가 있으면 해당 비속어의 단어 하나하나는 *로 대체되며, 이 결과를 리턴한다.
         



# 2. Profanity Usage Statistics(비속어 사용 통계)

## 2.1. 비속어를 보낸 시간을 찍는 부분
```java
public static HashMap<Integer, ArrayList<Timestamp>> week = new HashMap<Integer, ArrayList<Timestamp>>();
public static ArrayList<Timestamp> time = new ArrayList<Timestamp>();
```

사용자가 메세지를 보낼 때 비속어를 사용했으면 MainActivity에서 비속어를 보낸 시간을 저장할 수 있다.
비속어를 보낸 시각은 Timestamp 타입 원소를 갖는 Arraylist 에 저장하고 
HashMap에는 Wrapper 클래스 중 Integer 타입의 key값과 ArrayList<Timestamp>타입의 value를 저장한다.
key 값에는 시간중 일(dd)이, value 값에는 시간 전체(yy-mm-dd hh:mm:ss)를 담은 Array가 저장된다.


```java
if(badWordsFound.size()>0){
 time.add(new Timestamp(System.currentTimeMillis())); 
...}
```
사용자가 메세지를 보낼 때 비속어를 사용했으면 MainActivity에서 비속어를 보낸 시간을 저장할 수 있다.  
System.currentTimeMilis() 메소드가 실행하여, 비속어를 메세지로 적어보낸 시각을 "년-월-일 시:분:초" 구조로 리턴받고, 
이를 새로운 Timestamp 객체의 인자로 저장한 후 이 객체를 arraylist의 add() 메소드에 전달하여 time 에 저장한다. 
time 은 ArrayList로 사용자가 채팅방에 비속어가 적힌 메세지를 보낸 시각을 저장한다.

## 2.2. 날짜 얻는 부분
```java
String s;  
s = time.get(num).toString();  

StringTokenizer str = new StringTokenizer(s, " "); 
s = str.nextToken(); 

str = new StringTokenizer(s, "-"); 
for (int j = 0; j <3; j++) 
    s =str.nextToken();  

```
사용자가 비속어가 포함된 메세지를 보낸 날짜를 얻기 위해 Arraylist에 저장된 원소를 String으로 불러와 StringTokenizer로 자를 것이다.

구제적으로 설명하면 이는 ArrayList time에 저장해둔 원소를 String 타입 변수 s에 저장한 후, 
s와 " "을 인자로 가지는 StringTokenizer 객체를 생성하여 String 을 잘라 str에 저장하고, 그 중 첫번째 조각을 선택하여 String 타입 변수 s에 저장한다.

s와 "-"을 인자로 가지는 StringTokenizer 객체를 생성하여 Sring을 잘라 str에 저장하고, 
그 중 세번째 조각을 선택하여 String 타입 변수 s에 저장한 것이 date이다.

## 2.3. 해쉬맵에 넣는 부분

```java
if(!week.containsKey(date)) { 
    ArrayList<Timestamp> tmpList = new ArrayList<Timestamp>(); 
    tmpList.add(time.get(num));
    week.put(date, tmpList); 
}
```
오늘 처음으로 비속어가 적힌 메세지를 보내는 경우, 원소가 Timestamp 타입인 임시 Arraylist을 생성한다.
num번째 Timestamp타입의 원소를 꺼내서 위에서 찍어둔 시각을 받아와 임시 Arraylist에 저장한다. 
get(num)메소드를 이용하여 hashmap에서 key값이 num일 때 대응하는 value 값을 받아와 add() 메소드를 이용하여 tmpListRef에 저장하고
 put()메소드의 매개변수를 이용하여 date를 키로 tmpListRef을 value로 저장한다.

```java
else { 
    ArrayList<Timestamp> tmpListRef = week.get(date); 
    tmpListRef.add(time.get(num)); 
    week.put(date, tmpListRef); 
}
```
오늘 이미 비속어가 적힌 메세지를 보낸 경우, get(key) 메소드를 이용하여 value 값을 받아와 원소가 Timestamp 타입인 임시 Arraylist 에 저장한다. 
get()메소드를 이용하여 time에서 시각원소를 받아 add() 메소드를 이용하여 tmpListRef에 저장하고, put()메소드의 매개변수로 date를 키로 tmpListRef을 value를 저장한다.


## 2.4. 비속어 사용 통계 액티비티 생성

```java
public void callGraph(View view) {
        Intent GraphIntent = new Intent (this, SlangGraph.class);
        startActivity(GraphIntent);
    }
 ```

MainActivity의 callGraph() 메소드는 activity_main의 GRAPH 버튼과 연결되어 해당 버튼을 눌렀을 때 SlangGraph가 시작되도록 한다.


```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_slang_graph);
    showGraph();
}
 ```
SlangGraph가 호출되면 onCreate() 메소드 내에서 비속어 사용 통계량을 보여주는 showGraph() 메소드를 호출한다.


## 2.5. 비속어 통계 그래프 데이터 받기

```java
Timestamp today = new Timestamp(System.currentTimeMillis());
String key = today.toString();
StringTokenizer str = new StringTokenizer(key, " ");
key = str.nextToken();
str = new StringTokenizer(key, "-");
for(int i =0; i<3; i++){
    key = str.nextToken();
}
int k_value = Integer.parseInt(key);
 ```

showGraph() 메소드는 우선 Timestamp 객체를 생성해 메소드가 호출당한 시간을 알아낸다.
그 후 today의 값을 스트링으로 받아 StringTokenizer를 이용해 해당 시간의 날짜 부분을 key 변수에 대입한다.
그리고 key 변수의 String 값을 Integer로 변환한다.


```java
int[] array_x = new int[7];
for(int i=0; i<7; i++){
    array_x[i] = k_value-i;
}
int[] array_y = new int[7];
for(int i=0; i<7; i++){
    if(!MainActivity.week.containsKey(array_x[i])){
        array_y[i] = 0;
        break;
    }
    array_y[i] = MainActivity.week.get(array_x[i]).size();
}
 ```

그래프에서 최근 7일의 비속어 사용량을 보여주기 위해 k_value를 기준으로 그래프 x축에 사용될 값들을 배열 array_x에 대입한다.
그리고 MainActivity에서 생성된 HashMap에서 array_x의 값들을 key로 가지는 ArrayList value들의 사이즈를 배열 array_y에 순서대로 대입한다.


## 2.6. 비속어 사용 통계 그래프 생성

```java
implementation 'com.jjoe64:graphview:4.2.2'
 ```

비속어 사용 통계 그래프를 생성하기 위해 오픈 소스인 Android Graph Library GraphView를 사용한다.


 ```java
 graph_view = (com.jjoe64.graphview.GraphView) findViewById(R.id.graph);
 LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
         new DataPoint(array_x[6], array_y[6]),
         new DataPoint(array_x[5], array_y[5]),
         new DataPoint(array_x[4], array_y[4]),
         new DataPoint(array_x[3], array_y[3]),
         new DataPoint(array_x[2], array_y[2]),
         new DataPoint(array_x[1], array_y[1]),
         new DataPoint(array_x[0], array_y[0])
 });
 graph_view.addSeries(series);
  ```

GraphView 라이브러리를 사용해 선 그래프를 생성하고 이전에 만들어둔 배열 array_x와 array_y를 그래프의 DataPoint로 넣어준다.

 ```java
<com.jjoe64.graphview.GraphView
    android:id="@+id/graph"
    android:layout_width="315dp"
    android:layout_height="168dp"
    android:layout_marginStart="8dp"
    android:layout_marginTop="8dp"
    android:layout_marginEnd="8dp"
    android:layout_marginBottom="8dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintHorizontal_bias="0.849"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintVertical_bias="0.498" />
  ```

그래프를 액티비티에서 볼 수 있도록 activity_slang_graph.xml에도 그래프 요소를 추가한다.




# 3. User Interface(사용자 인터페이스)

## 3.1. 로딩 화면

### 3.1.1. 스플래쉬 액티비티 추가

```java
public class SplashActivity extends Activity {

    // Time for loading screen to float (milliseconds)
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    // Called when activity is first generated
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        // Run menu activity behind SPLASH_DISPLAY_LENGTH and exit
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // Start SplashActivity and kill loading screen
                Intent mainIntent = new Intent(SplashActivity.this,MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
```

int형 변수 SPLASH_DISPLAY_LENGTH를 선언한다. 이 변수는 로딩창이 띄워져 있는 시간을 밀리초단위로 저장한다.
onCreat(Bundle icicle)함수를 이용해 액티비티가 수행될 때 할 일을 선언한다.
이 때, layout.activity_splash를 통하여 스플래쉬 액티비티가 앞서 선언한 변수 시간 만큼 띄워진다.
이후 Intent를 통해 스플래쉬 액티비티에서 메인액티비티로 넘어가게 된다.

### 3.1.2. 스플래쉬 레이아웃 추가

```java
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" android:layout_width="fill_parent"
    android:layout_height="fill_parent">

    <ImageView
        android:id="@+id/splashscreen"
        android:layout_width="384dp"
        android:layout_height="match_parent"
        android:src="@drawable/icon5" /> // this screen use image icon5

    // not used
    <TextView android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:text="Hello World, splash"/>
</LinearLayout>
```

ImageView 내 src변수에 스플래쉬 화면으로 이용하고 싶은 이미지를 선택한다.
로딩중임을 알리는 icon5.png가 사용되었다.

### 3.1.2. AndroidManifest.xml 내 시작 액티비티 변경

```java
<activity android:name=".SplashActivity" >  // Notify that the activity is the first activity after declaring SplashActivity
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name=".MainActivity" />
        <activity android:name=".SignInActivity" />
        <activity android:name=".ChatRoomActivity" />
        <activity android:name=".SlangGraph">
        </activity>
```

<intent-filter> 를 안에 갖고 있는 액티비티는 프로그램이 시행될 때 제일 먼저 수행되는 액티비티이다.
기존의 MainActivity 속에 있던 <intent-filter> 부분을 SplashActivity를 선언한 후 그 안으로 옮긴다.

## 3.2. 테마 컬러 설정

```java
<?xml version="1.0" encoding="utf-8"?>
<resources>
<color name="colorPrimary">#ffbe3c</color> // menu bar color
<color name="colorPrimaryDark">#000000</color> // top bar color
<color name="colorAccent">#2E7D32</color>
<color name="colorTitle">#ffffff</color>
<color name="graphButton">#FFFFDD00</color>
<color name="white" >#ffffff</color>
</resources>
```

기존에 설정되어 있는 테마에 따라서 컬러를 변경하면 상단바와 메뉴바의 색상을 변경할 수 있다.
values > colors.xml 내의 컬러 변수들을 변경하여 사용한다.
이 때, "colorPrimary"는 메뉴바의 색상을 "colorPrimaryDark"는 상단바의 색상을 나타낸다.

## 3.3. 아이콘 및 기타 UI

### 3.3.1. 이미지 및 폰트 파일 추가

이미지를 아이콘과 애플리캐이션 대표 이미지 등에 사용하기 위하여 res > drawble 폴더 내에 이미지를 png 파일로 넣어준다.
마찬가지로 폰트를 res 폴더 내 새로운 font 폴더를 생성해 otf 파일로 넣어준다.
이 파일들은 다른 곳에서 접근하여 사용될 수 있다.

### 3.3.2. AndroidManifest.xml 내 변수값 변경

```java
android:allowBackup="true"
android:icon="@mipmap/ic_launcher"
android:label="삐비톡"
android:supportsRtl="true"
android:roundIcon="@mipmap/ic_launcher_round"
android:theme="@style/AppTheme">
// label is icon application name and roudIcon is application icon image
```

AndroidManifest.xml 내 변수들을 변경하여 이 기능들을 수행한다.
기존의 label 값을 변경하여 '삐비톡' 애플리캐이션의 이름을 변경한다.
그리고 roundIcon 사용을 위해 앞서 선언한 이미지를 res > image Assert 메뉴를 통해 임포트 시킨다.

### 3.3.3. activity_main.xml 내 버튼 글씨체 및 색상 변경

```java
<Button
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="37dp"
        android:layout_alignParentEnd="true"
        android:layout_alignParentTop="true"
        android:layout_marginEnd="9dp"
        android:layout_marginTop="15dp"
        android:background="#FF9333 "
        android:fontFamily="@font/nanumsquare_eb"
        android:onClick="callGraph"
        android:text="Graph" />
```

이 버튼은 이용자가 욕설 사용량을 그래프로 확인하기 위해 누르는 버튼이다.
버튼의 크기와 위치를 변수들의 값을 변경해 알맞게 조정하고, fontFamily를 이용해 원하는 글씨체를 적용시킨다.
마지막으로, background는 버튼의 색상을 나타내는 변수로 이를 변경해 버튼의 색상을 바꿀 수 있다.



# 3. 설치 방법 및 사용법
* [bbi-bi-talk.apk](bbi-bi-talk.apk) 파일을 안드로이드 기기에 다운로드하고, 실행하여 설치한다.

* 삐- 비속어 방지톡 어플리케이션 설치 후 google 계정으로 로그인하여 사용한다. 

* 채팅방에서 욕설을 보내면 필터링하여 전송된다. 

* 채팅방에서 GRAPH 버튼을 누르면 자신의 비속어 사용 통계를 볼 수 있다.


# 4. 사용 오픈소스

* Firebase 안드로이드 메신저 오픈소스
https://github.com/firebase/friendlychat-android, Apache License 2.0

* 비속어 필터링 오픈소스
https://github.com/souwoxi/Profanity

* Graph View 통계량 오픈소스
https://github.com/jjoe64/GraphView, Apache License 2.0


# 5. 라이센스
See [LICENSE](LICENSE), Apache License 2.0


# 6. 개발자 정보

* 1515004 권나현(KwonNH): 비속어 필터링 기능 개발, Git 관리
         - NahyunChat1, 2, 3: 비속어 필터링 기능
* 1771022 문효진(hyojin530): 비속어 통계량 기능 개발, 발표자료, 중간발표자
         - YeaunChat2, statistics_2 : 비속어 통계량 기능
* 1771046 이혜진(leeheajin): UI 디자인 담당, 삐비톡 아이콘 디자인, 기말발표자
         - NahyunChat4: 사용자 인터페이스 
* 1771105 조예은(yjo5252): 비속어 통계량 기능 개발, 발표자료, 기말발표자
         - YeaunChat2, statistics_2 : 비속어 통계량 기능
* * *

Ewha Womans University
2018-2 Open Software Platform
Team Project
-Team 13
