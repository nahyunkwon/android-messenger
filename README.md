"Android Messanger with profanity filtering & profanity usage statistics"
=============

삐- 비속어 방지톡
------------

>삐- 비속어 방지톡은 메신저 사용자가 보내는 비속어를 필터링하고, 
얼마나 비속어를 사용했는지 통계로 보여 주어 사용자에게 비속어 사용에 대한 경각심을 주고, 
나아가 메신저를 통한 사이버 불링 등의 범죄를 방지하고자 하는 안드로이드 메신저이다.



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
         </code></pre>
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
InputStreamReader is = new InputStreamReader(getAssets()
                     .open("Word_Filter.csv")); //get bad word database from assets folder
```

loanBadWords() 메소드는 assets 폴더의 비속어 데이터를 불러온다.
비속어 데이터는 csv 파일 형식으로 저장한다.

```java
// Make sure there are no capital letters in the spreadsheet
allBadWords.put(word.replaceAll(" ", "").toLowerCase(), ignore_in_combination_with_words);
```
                     
모든 비속어는 allBadWords 해시맵에 저장된다.              


## 1.4. 비속어 필터링

```java
// remove leetspeak
modifiedInput = modifiedInput.replaceAll("1", "i").replaceAll("!", "i").replaceAll("3", "e").replaceAll("4", "a")
    .replaceAll("@", "a").replaceAll("5", "s").replaceAll("7", "t").replaceAll("0", "o").replaceAll("9", "g");
```

getCensoredText()에서는 먼저 input에서 leetspeak을 제거한다.(영어 비속어의 경우)

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
여기서 largestWordLength는 loanBadWords()에서 계산된, 비속어 데이터를 정리할 때 가장 긴 비속어의 길이를 의미한다.
         
         
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






# 3. User Interface(사용자 인터페이스)






## 4. 사용 오픈소스

* Firebase 안드로이드 메신저 오픈소스
https://github.com/KwonNH/android-messenger

* 비속어 필터링 오픈소스
https://github.com/souwoxi/Profanity

* Graph View 통계량 오픈소스
https://github.com/jjoe64/GraphView


## 5. 팀원별 역할

* 1515004 권나현: 비속어 필터링 기능 개발

* * *

Ewha Womans University
2018-2 Open Software Platform
Team Project
-Team 13