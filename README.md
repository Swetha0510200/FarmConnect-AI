# FarmConnect AI ? AI-Powered Farmer-to-Buyer Marketplace and Smart Supply Chain Platform

**Smart India Hackathon 2026 Problem Statement: SIH26033**  
**Category:** Agriculture, FoodTech & Rural Development  
**Technology:** Java 17, Spring Boot 3, Spring Data JPA, Spring Security, MySQL 8, Thymeleaf, HTML5, CSS3, JavaScript, Chart.js, Maven  

---

LIVE DEMO

 https://farmconnect-ai-8.onrender.com

##  Project Overview

**FarmConnect AI** is a digital agricultural marketplace and transparent coordination platform that directly connects farmers and Farmer Producer Organizations (FPOs) with verified bulk buyers (wholesalers, retailers, food processors, exporters, and institutions).

In conventional agricultural trade, smallholder farmers depend on multiple intermediaries and lack visibility into:
1. **Real-time demand**: Which buyers are actively looking for their specific crop.
2. **Fair pricing**: Current prevailing mandi / APMC market rates vs. buyer bids.
3. **Suitability**: Whether a buyer's quantity, price budget, quality grade, and timeline match what the farmer has available.
4. **Geographic feasibility**: How far the buyer is located and delivery coordination.
5. **Order transparency**: Live progress of placed orders, direct payment receipts, and delivery stages.

FarmConnect AI addresses these challenges with a clean, farmer-friendly web interface that hides technical complexity and presents clear, transparent insights:
> *"ABC Wholesale Foods is looking for 500 kg of Tomato (25 km away) ? ?24??26/kg offered ? 94% Suitable Match"*

---

## ??? Core Operating Principles & Disclaimers

1. **Direct P2P Transactions**: FarmConnect AI is a digital marketplace & coordination platform. FarmConnect does **not** process, collect, or hold payments. Payment happens directly between the buyer and farmer via **Cash, UPI, or Bank Transfer**.
2. **Transportation Coordination**: FarmConnect does **not** operate a transport fleet. Transportation is arranged directly between the farmer and buyer (Farmer delivery, Buyer pickup, or Mutual arrangement).
3. **Real Dynamically Stored Data**: There are **no hardcoded fake records** or mock startup lists. All users, listings, requirements, orders, and market records come from MySQL and real external government data feeds.
4. **Explainable AI Matching**: Suitability scores (e.g. 94%) are accompanied by transparent, readable reasons (Crop match, Quantity proximity, Budget range compatibility, Geographic distance in km, Availability date overlap).

---

## ?? Key Features

### ????? Farmer Module
- **Farmer Registration & Login**: Simple registration with farm details (village, district, state, farm size, farming type).
- **Farmer Dashboard**: Live summary of active crops, recent orders, market price ticker, and top buyer opportunities.
- **Crop Listing Management**: Add, edit, deactivate, and delete crop listings (crop name, quantity, unit, expected price, quality grade, availability dates, pickup village).
- **Interactive Price Comparison**: Live feedback comparing the farmer's expected rate with prevailing APMC market rates.
- **Smart Buyer Matching ("Suitable Buyers")**: AI matching engine ranking buyer requirements with transparent match reasons and distance calculations.
- **Order Management & Direct Payment Confirmation**: Receive orders, accept/reject bids, advance order states (`PLACED` ? `ACCEPTED` ? `PREPARING` ? `READY_FOR_PICKUP` ? `DELIVERED` ? `COMPLETED`), and mark direct payments as `RECEIVED`.
- **Sales Analytics**: Dynamic charts powered by Chart.js displaying real sales revenue, quantity sold, and crop-wise distribution.

### ?? Buyer Module
- **Buyer Registration & Business Verification**: Register business profile (Wholesaler, Retailer, Processor, Exporter, Hotel) with office/warehouse address.
- **Search & Filter Crops**: Multi-filter crop search by crop name, district, price range, quantity, and quality grade.
- **Post Crop Requirements**: Specify required crop, required quantity, target budget range (min?max price), quality grade, and delivery window.
- **Matched Farmers**: Instant discovery of local farmers with matching harvest supplies.
- **Direct Order Placement**: Place orders with custom quantity, agreed rate, and order notes.
- **Live Order Tracking**: Visual progress bar tracking order stages in real time.
- **Favorite Listings**: Save and manage bookmarked crop listings.

### ?? Demand Insights & Market Prices
- **Live APMC / Mandi Market Prices**: Search and filter market rates across Indian districts and states with retry functionality against official open data feeds.
- **Real Demand vs Supply Insights**: Database-driven aggregation comparing listed farmer supply against buyer requirement demand by commodity.

### ??? System Administration Module
- **Admin Dashboard**: System health metrics, user statistics, active listings count, and completed trade volume.
- **User Governance**: Moderate and activate/deactivate user accounts.
- **Buyer Verification**: Review buyer credentials and grant verified badges.
- **Listing & Requirement Moderation**: Review and moderate marketplace postings.
- **Platform Analytics**: Visual analytics of overall platform trade activity.

---

## ??? System Architecture & Technology Stack

```
                     ????????????????????????????????????????
                     ?   Browser (Responsive UI / Desktop)   ?
                     ?  HTML5 + CSS3 + Bootstrap 5 + JS     ?
                     ????????????????????????????????????????
                                        ? HTTP / HTTPS (CSRF Protected)
                                        ?
                     ????????????????????????????????????????
                     ?    Spring Boot 3 (Java 17) MVC       ?
                     ?  Spring Security (Role-Based Auth)   ?
                     ????????????????????????????????????????
                                        ?
           ???????????????????????????????????????????????????????????
           ?                            ?                            ?
?????????????????????????   ?????????????????????????   ?????????????????????????
? Smart Matching Engine ?   ? Market Price Service  ?   ? Geolocation Service   ?
? Multi-factor Scoring  ?   ? Data.gov.in / Mandi   ?   ? Haversine Distance    ?
?????????????????????????   ?????????????????????????   ?????????????????????????
           ?                            ?                            ?
           ???????????????????????????????????????????????????????????
                                        ? Spring Data JPA / Hibernate
                                        ?
                     ????????????????????????????????????????
                     ?           MySQL 8 Database           ?
                     ?           farmconnect_db             ?
                     ????????????????????????????????????????
```

- **Backend**: Java 17, Spring Boot 3.2.5, Spring MVC, Spring Data JPA, Spring Security 6
- **Database**: MySQL 8.0 (`farmconnect_db`)
- **Frontend**: Thymeleaf Template Engine, HTML5, CSS3, Bootstrap 5.3, Bootstrap Icons, Chart.js
- **Build Tool**: Apache Maven 3.9+

---

## ??? Relational Database Schema

| Table Name | Description | Key Columns |
|---|---|---|
| `users` | User accounts & authentication | `id`, `name`, `email`, `mobile`, `password`, `role`, `account_status`, `created_at` |
| `farmer_profiles` | Farmer profile & farm info | `id`, `user_id`, `farm_name`, `farm_size`, `village`, `district`, `state`, `latitude`, `longitude`, `farming_type` |
| `buyer_profiles` | Buyer company & verification | `id`, `user_id`, `business_name`, `business_type`, `address`, `district`, `state`, `verification_status` |
| `crop_listings` | Farmer crop harvest listings | `id`, `farmer_id`, `crop_name`, `quantity`, `unit`, `expected_price`, `quality_grade`, `available_from`, `available_until`, `location`, `status` |
| `buyer_requirements`| Buyer purchasing requests | `id`, `buyer_id`, `crop_name`, `required_quantity`, `minimum_price`, `maximum_price`, `required_from`, `required_until`, `location`, `status` |
| `orders` | Direct marketplace orders | `id`, `order_number`, `buyer_id`, `farmer_id`, `crop_listing_id`, `crop_name`, `quantity`, `agreed_price`, `total_amount`, `status` |
| `payment_records` | P2P direct payment tracking | `id`, `order_id`, `payment_method` (CASH/UPI/BANK_TRANSFER), `payment_status` (PENDING/RECEIVED), `received_date` |
| `delivery_arrangements`| Logistics coordination | `id`, `order_id`, `responsibility` (BUYER/FARMER/MUTUAL), `pickup_location`, `delivery_location`, `distance`, `status` |
| `market_prices` | Official mandi price records | `id`, `crop_name`, `market_name`, `district`, `state`, `price`, `date`, `source` |
| `saved_listings` | Bookmarked favorite crops | `id`, `user_id`, `crop_listing_id`, `created_at` |
| `notifications` | In-app user notifications | `id`, `user_id`, `title`, `message`, `type`, `link`, `is_read`, `created_at` |

---

## ?? How to Run Locally

### Prerequisites
1. **Java Development Kit (JDK 17+)** installed.
2. **Apache Maven 3.8+** installed.
3. **MySQL Server 8.0+** running on `localhost:3306`.

### Step 1: Create Database
Open MySQL CLI or phpMyAdmin and run:
```sql
CREATE DATABASE IF NOT EXISTS farmconnect_db;
```

### Step 2: Configure Database Credentials (Optional)
If your MySQL root password is not blank, edit `src/main/resources/application.properties` or set environment variables:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/farmconnect_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 3: Build and Run Application
```bash
# Clean and compile
mvn clean install

# Run Spring Boot application
mvn spring-boot:run
```

The application will start on:
?? **`http://localhost:8080`**

---

## ?? Testing the Complete Workflow

### 1. Default Administrator Account
An initial secure administrator account is initialized on first startup:
- **URL**: `http://localhost:8080/auth/login`
- **Email**: `admin@farmconnect.ai`
- **Password**: `Admin@FarmConnect2026`
- **Actions**: View platform analytics, verify registered buyers, manage listings, monitor trade volume.

### 2. Farmer Workflow Test
1. Go to `http://localhost:8080/auth/register-farmer`.
2. Register a new farmer (e.g. *Ramesh Kumar*, Mobile: `9876543211`, Email: `ramesh@farm.com`, District: *Tiruvallur*, State: *Tamil Nadu*).
3. Log in with the registered credentials.
4. Click **Add New Crop** ? Enter *Tomato*, Quantity: *500 kg*, Price: *?24/kg*, Available dates.
5. Notice the live **Market Price Comparison** banner giving instant mandi context.
6. Open **Suitable Buyers** to review AI suitability scores, match factors, and distance.
7. Under **My Orders**, accept incoming buyer orders, coordinate delivery, and mark payment as received.
8. Open **Sales Analytics** to view Chart.js revenue summaries.

### 3. Buyer Workflow Test
1. Go to `http://localhost:8080/auth/register-buyer`.
2. Register a buyer (e.g. *ABC Wholesale Foods*, Mobile: `9876543212`, Email: `procurement@abcwholesale.com`, District: *Chennai*, State: *Tamil Nadu*).
3. Log in and go to **Post Requirement** ? Post a requirement for *Tomato*, *500 kg*, Budget: *?22??26/kg*.
4. Go to **Matched Farmers** to see compatible farmer listings.
5. Go to **Search Crops** ? Click on a listed crop ? Click **Place Order**.
6. Follow the live **Order Tracking Timeline** as the farmer accepts and updates delivery.

---

## ?? SIH 2026 SIH26033 Alignment

| SIH26033 Requirement | FarmConnect AI Implementation |
|---|---|
| Direct Farmer-Buyer Linkage | Role-based marketplace eliminating commission middlemen |
| Farmer-Friendly Simplicity | Non-technical language ("Suitable Buyer" vs "ML Model"), clean mobile-responsive layout |
| Transparent Suitability Scoring | Multi-factor compatibility engine (Crop, Quantity, Price, Distance, Dates, Quality) with bulleted reasons |
| Real Market Price Awareness | Direct mandi rate lookups and expected vs market price comparison |
| Real Data Persistence | Strict MySQL 8 relational schema with zero fake hardcoded records |
| Direct Settlement Clarity | Prominent disclaimers explaining direct P2P payments and delivery coordination |
| Complete MVC Quality | Clean Java 17 + Spring Boot 3 enterprise structure ready for university demonstration |

---

## ?? License
Developed for Smart India Hackathon 2026 (SIH26033) & College Computer Science Project.
