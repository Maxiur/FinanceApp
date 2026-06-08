# FinanceApp - Zarządzanie Budżetem Osobistym

FinanceApp to aplikacja typu REST API służąca do zarządzania osobistymi finansami (przychody i wydatki) przypisanymi do różnych kont bankowych. Zawiera ona również wbudowane limity budżetowe.

---

## Jak uruchomić aplikację lokalnie

### Wymagania wstępne
Do uruchomienia projektu wymagane są:
* **Java 25** (lub kompatybilna JDK)
* **Docker** oraz **Docker Compose**

---

### Krok po kroku

#### 1. Uruchomienie Bazy Danych (PostgreSQL)
Upewnij się, że silnik Docker jest włączony, a następnie uruchom bazę danych oraz pgAdmin za pomocą pliku `docker-compose.yaml`:
```bash
docker-compose up -d
```
Spowoduje to uruchomienie:
* Bazy danych PostgreSQL na porcie `5432` (baza: `budget_db`, użytkownik: `admin`)
* Narzędzia pgAdmin na porcie `5050`

#### 2. Konfiguracja zmiennych środowiskowych
Zmienne połączenia do bazy danych są pobierane automatycznie z pliku `.env` znajdującego się w katalogu głównym projektu. Plik ten zawiera m.in.:
```env
DB_NAME=budget_db
DB_USER=admin
DB_PASSWORD=zuzia123
DB_PORT=5432
DB_URL=jdbc:postgresql://localhost:5432/budget_db
```

#### 3. Uruchomienie Aplikacji
Uruchom serwer Spring Boot za pomocą Gradle Wrapper:
```bash
# Na Windows:
.\gradlew bootRun

# Na macOS/Linux:
./gradlew bootRun
```
Aplikacja zostanie uruchomiona na porcie **`9091`**.

#### 4. Uruchomienie Testów
Aby wyczyścić kompilację i odpalić pełne testy (jednostkową oraz integracyjną):
```bash
# Na Windows:
.\gradlew clean test

# Na macOS/Linux:
./gradlew clean test
```
*Uwaga: Testy integracyjne używają biblioteki **Testcontainers**, która automatycznie uruchamia tymczasową, odizolowaną bazę danych PostgreSQL w Dockerze na czas wykonywania testów. Do uruchomienia testów wymagane jest jedynie działanie silnika Docker w tle (np. Docker Desktop).*

---

## Dokumentacja OpenAPI & Swagger
Dokumentacja API wygenerowana automatycznie przy pomocy Springdoc jest dostępna pod adresem:
👉 **[http://localhost:9091/swagger-ui/index.html](http://localhost:9091/swagger-ui/index.html)**

---

## Opis Endpointów API

### Konta (`/api/v1/accounts`)
* **`GET /api/v1/accounts`** - Pobiera listę wszystkich kont.
* **`POST /api/v1/accounts`** - Tworzy nowe konto (wymagana unikalna nazwa).
* **`GET /api/v1/accounts/{idOrName}`** - Szczegóły konta wraz z saldem.
* **`DELETE /api/v1/accounts/{idOrName}`** - Usuwa konto pod warunkiem, że nie posiada przypisanych transakcji (zwraca `422 Unprocessable Content` w przypadku posiadania transakcji).

### Transakcje (`/api/v1/accounts/{idOrName}/transactions`)
* **`GET /api/v1/accounts/{idOrName}/transactions`** - Lista transakcji danego konta.
  * *Parametry opcjonalne (filtry):* `?from=2026-01-01&to=2026-12-31&category=Jedzenie`
* **`POST /api/v1/accounts/{idOrName}/transactions`** - Dodaje nową transakcję. Automatycznie aktualizuje saldo konta. Zwraca pole `warning`, jeżeli kwota wydatków w bieżącym miesiącu przekroczy limit dla wybranej kategorii.
* **`DELETE /api/v1/transactions/{id}`** - Usuwa transakcję i automatycznie wycofuje jej wpływ na saldo konta.
* **`GET /api/v1/accounts/{idOrName}/transactions/export`** - Eksportuje historię transakcji do pliku CSV.

### Podsumowanie (`/api/v1/summary`)
* **`GET /api/v1/summary`** - Zwraca sumaryczne przychody, wydatki oraz wydatki pogrupowane po kategoriach.
* **`GET /api/v1/summary/limits`** - Wypisuje limity ustalone dla poszczególnych kategorii w pliku `resources/application.yml`.
---