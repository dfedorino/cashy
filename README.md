<!-- TOC -->
* [Описание](#описание)
* [Архитектура](#архитектура)
* [Автоматизация](#автоматизация)
* [Контроль качества](#контроль-качества)
* [Запуск](#запуск)
  * [Средний уровень сложности](#средний-уровень-сложности)
  * [Высокий уровень сложности](#высокий-уровень-сложности)
<!-- TOC -->ino_cashy&metric=coverage)](https://sonarcloud.io/summary/new_code?id=dfedorino_cashy)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=dfedorino_cashy&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=dfedorino_cashy)

# Запуск
Необходимо для запуска:
* Java 21

# Примеры команд
Скачать jar файл:
[cashy-1.0.0.jar](https://github.com/dfedorino/cashy/releases/download/v1.0.0/cashy-1.0.0.jar)
Запустить в терминале в директории, в которую скачан jar:
```
java -jar cashy-1.0.0.jar
```

После этой команды в консоли появится лог и список поддерживаемых комманд:
```
>> Cashy Project <<
Available commands:
* category_create : Create a new category with the given budget and alert threshold, example: 'category_create food 30000 alert 80'
* category_edit : Edit an existing category name or budget, example: 'category_edit food -n groceries
category_edit food -l 5000'
* income : Perform an income operation, example: 'income salary 100000'
* login : Login with user's login and password, example: 'login my_login -p my_password'
* quit : Exit, example: 'quit'
* stats : Fetch budget statistics, example: 'stats -c groceries taxi'
* withdraw : Perform a withdrawal operation, example: 'withdraw groceries 5000'
```
Авторизация пользователей
```
>> login test -p test
 Login successful!
```
Управление финансами:
* добавление доходов
```
>> income Salary 20000
Income operation performed successfully!

>> income Salary 40000
Income operation performed successfully!

>> income Bonus 3000
Income operation performed successfully!
```
* создание категорий
```
>> category_create Groceries 4000 alert 10
Category created successfully!

>> category_create Entertainment 3000 alert 50
Category created successfully!

>> category_create Services 2500 alert 60
Category created successfully!
```
* добавление расходов:
```
>> withdraw Groceries 300
Withdrawal operation performed successfully!

>> withdraw Groceries 500
Withdrawal operation performed successfully!
Warning! Spending threshold 10 reached!

>> withdraw Entertainment 3000
Withdrawal operation performed successfully!
Warning! Category limit reached!

>> withdraw Services 3000
Withdrawal operation performed successfully!
Warning! Category limit exceeded!

>> withdraw Taxi 1500
Withdrawal operation performed successfully!
```

Как видно выше, пользователь получает уведомления, когда:
* траты по категории превысили 10% (установленный пользователем порог)
* бюджет категории достигнут
* бюджет категории превышен

Вывод информации и статистика:
```
>> stats
Statistics fetched successfully!
Доходы:
┌───────────────────────────────────────┬──────────────────────────────────────┐
│Категория                              │Добавлено                             │
├───────────────────────────────────────┼──────────────────────────────────────┤
│Salary                                 │60000.00                              │
│Bonus                                  │3000.00                               │
├───────────────────────────────────────┼──────────────────────────────────────┤
│Итого                                  │63000.00                              │
└───────────────────────────────────────┴──────────────────────────────────────┘
Расходы:
┌───────────────────┬───────────────────┬───────────────────┬──────────────────┐
│Категория          │Израсходовано      │Бюджет             │Остаток бюджета   │
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│Groceries          │800.00             │4000.00            │3200.00           │
│Entertainment      │3000.00            │3000.00            │0.00              │
│Services           │3000.00            │2500.00            │-500.00           │
│Taxi               │1500.00            │-                  │-                 │
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│Итого              │8300.00            │-                  │-                 │
└───────────────────┴───────────────────┴───────────────────┴──────────────────┘
```
Сохранение и загрузка данных работает, можно проверить следующим образом:
```
>> quit

>> java -jar cashy-v1.0.0.jar

>> login test -p test
Login successful!

>> stats -c Groceries Entertainment
stats -c Groceries Entertainment
Statistics fetched successfully!
Доходы:
┌───────────────────────────────────────┬──────────────────────────────────────┐
│Категория                              │Добавлено                             │
├───────────────────────────────────────┼──────────────────────────────────────┤
│Salary                                 │60000.00                              │
│Bonus                                  │3000.00                               │
├───────────────────────────────────────┼──────────────────────────────────────┤
│Итого                                  │63000.00                              │
└───────────────────────────────────────┴──────────────────────────────────────┘
Расходы:
┌───────────────────┬───────────────────┬───────────────────┬──────────────────┐
│Категория          │Израсходовано      │Бюджет             │Остаток бюджета   │
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│Groceries          │800.00             │4000.00            │3200.00           │
│Entertainment      │3000.00            │3000.00            │0.00              │
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│Итого              │3800.00            │-                  │-                 │
└───────────────────┴───────────────────┴───────────────────┴──────────────────┘
```
Как видно выше, после выхода из приложения и логина снова, отобразились те же расходы и доходы.
Также видно, что функция выбора категорий работает корректно - отображены только выбранные категории расходов

Валидация ввода:
Некорректная команда
```
>> wrong command
!!! Invalid command !!!
Available commands:
* category_create : Create a new category with the given budget and alert threshold, example: 'category_create food 30000 alert 80'
* category_edit : Edit an existing category name or budget, example: 'category_edit food -n groceries
category_edit food -l 5000'
* income : Perform an income operation, example: 'income salary 100000'
* login : Login with user's login and password, example: 'login my_login -p my_password'
* quit : Exit, example: 'quit'
* stats : Fetch budget statistics, example: 'stats -c groceries taxi'
* withdraw : Perform a withdrawal operation, example: 'withdraw groceries 5000'
```
Валидация параметров команды:
```
withdraw abc def
Amount invalid: 'def'
```
Редактирование бюджетов и категорий:
```
>> category_edit Groceries -n Food
Category edited successfully!

>> category_edit Food -l 5000
Category edited successfully!

>> stats
stats
Statistics fetched successfully!
Доходы:
┌───────────────────────────────────────┬──────────────────────────────────────┐
│Категория                              │Добавлено                             │
├───────────────────────────────────────┼──────────────────────────────────────┤
│Salary                                 │60000.00                              │
│Bonus                                  │3000.00                               │
├───────────────────────────────────────┼──────────────────────────────────────┤
│Итого                                  │63000.00                              │
└───────────────────────────────────────┴──────────────────────────────────────┘
Расходы:
┌───────────────────┬───────────────────┬───────────────────┬──────────────────┐
│Категория          │Израсходовано      │Бюджет             │Остаток бюджета   │
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│Food               │800.00             │5000.00            │4200.00           │
│Entertainment      │3000.00            │3000.00            │0.00              │
│Services           │3000.00            │2500.00            │-500.00           │
│Taxi               │1500.00            │-                  │-                 │
├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
│Итого              │8300.00            │-                  │-                 │
└───────────────────┴───────────────────┴───────────────────┴──────────────────┘
```
Как видно выше, изменения названия и бюджета категории отобразилось в статистике

Экспорт/импорт отчётов в формате JSON:
```
>> stats -e
>> JSON exported successfully to: /path/to/directory/with/jar/stats.json
Statistics fetched successfully!
...
```
Как видно, в консоли отобразился путь к файлу, 
который генерируется в той же директории, где лежит .jar со следующим содержанием:
```
>> cat .\stats.json
{
  "totalIncomeAmount": 63000.00,
  "incomeCategories": [
    {
      "categoryName": "Salary",
      "currentBalance": 60000.00
    },
    {
      "categoryName": "Bonus",
      "currentBalance": 3000.00
    }
  ],
  "totalExpenseAmount": 8300.00,
  "expenseCategories": [
    {
      "categoryName": "Food",
      "currentBalance": 800.00,
      "limit": 5000.00,
      "remainingBalance": 4200.00
    },
    {
      "categoryName": "Entertainment",
      "currentBalance": 3000.00,
      "limit": 3000.00,
      "remainingBalance": 0.00
    },
    {
      "categoryName": "Services",
      "currentBalance": 3000.00,
      "limit": 2500.00,
      "remainingBalance": -500.00
    },
    {
      "categoryName": "Taxi",
      "currentBalance": 1500.00
    }
  ]
}
```
