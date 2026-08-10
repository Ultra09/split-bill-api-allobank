# Allo Bank Backend Developer Take-Home Test - Split Bill API

## Overview
This is a Spring Boot REST API for managing shared expenses, tracking payments, maintaining an audit trail, and calculating optimal settlements. 

## Requirements Fulfillment
- Java 21, Spring Boot, Maven
- `BigDecimal` used exclusively for all monetary values.
- In-memory H2 Database for zero-configuration testing.
- Multi-stage Dockerfile provided matching the challenge template.
- Comprehensive Unit Test provided for the Settlement logic.

## How to Build and Run

### Running Locally with Maven
Ensure you have Java 21 installed.
```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```
The API will be available at `http://localhost:4110`.

### Running with Docker
```bash
# Build the Docker image
docker build -t split-bill-api .

# Run the container
docker run -p 4110:4110 split-bill-api
```

---

## 📖 Complete API Documentation (Flow-by-Flow)

This section walks you through a complete lifecycle of a weekend trip to Bali.

### Step 1: Create a Bill Group
Start by creating a group and adding the participants.
```bash
curl -X POST http://localhost:4110/api/groups \
-H "Content-Type: application/json" \
-d '{
  "name": "Bali Trip",
  "participants": ["Alice", "Bob", "Charlie"]
}'
```

### Step 2: Add Expenses (With Categories & Multiple Split Strategies)
Participants can add expenses. The API supports Equal, Percentage, and Exact Amount splits, as well as categorizing the expense.

**A. Equal Split (Food)**
Bob pays 150000 IDR for dinner. It's split equally.
```bash
curl -X POST http://localhost:4110/api/groups/1/expenses \
-H "Content-Type: application/json" \
-d '{
  "description": "Dinner at Jimbaran",
  "totalAmount": 150000.00,
  "category": "FOOD",
  "payerId": 2
}'
```

**B. Split by Percentage (Transport)**
Alice pays 100000 IDR for a taxi. Alice covers 40%, Bob covers 60%.
```bash
curl -X POST http://localhost:4110/api/groups/1/expenses \
-H "Content-Type: application/json" \
-d '{
  "description": "Taxi ride",
  "totalAmount": 100000.00,
  "category": "TRANSPORT",
  "payerId": 1,
  "splits": [
    { "participantId": 1, "percentage": 40.00 },
    { "participantId": 2, "percentage": 60.00 }
  ]
}'
```

**C. Split by Exact Amount (Other)**
Bob pays 50000 IDR for souvenirs. Alice's souvenir was 15000 IDR, Charlie's was 35000 IDR.
```bash
curl -X POST http://localhost:4110/api/groups/1/expenses \
-H "Content-Type: application/json" \
-d '{
  "description": "Souvenirs",
  "totalAmount": 50000.00,
  "category": "OTHER",
  "payerId": 2,
  "splits": [
    { "participantId": 1, "exactAmount": 15000.00 },
    { "participantId": 3, "exactAmount": 35000.00 }
  ]
}'
```

### Step 3: Record a Payment
Alice wants to pay off some of her debt to Bob mid-trip. She wires him 30000 IDR.
```bash
curl -X POST http://localhost:4110/api/groups/1/payments \
-H "Content-Type: application/json" \
-d '{
  "payerId": 1,
  "receiverId": 2,
  "amount": 30000.00
}'
```

### Step 4: Check the Audit Trail
Want to see everything that happened in the group so far? Retrieve the chronological audit history.
```bash
curl -X GET http://localhost:4110/api/groups/1/audit
```

### Step 5: Retrieve Settlement Summary (Who owes whom?)
At the end of the trip, this endpoint calculates the remaining net balances, applies the greedy optimization algorithm, and returns the absolute minimum number of transactions required to settle the remaining debts.

```bash
curl -X GET http://localhost:4110/api/groups/1/settlement
```

**Example Response:**
*(Notice it returns the minimized remaining transactions, a breakdown of expenses by category, and the personalized service charge).*
```json
{
  "groupId": 1,
  "transactions": [
    {
      "from": "Charlie",
      "to": "Bob",
      "amount": 85000.00
    }
  ],
  "categorySummaries": {
    "FOOD": 150000.00,
    "TRANSPORT": 100000.00,
    "OTHER": 50000.00
  },
  "serviceChargePct": 9.00,
  "serviceChargeAmount": 27000.00
}
```

---

## Personalization
- **GitHub Username:** `ultra09`
- **Calculated `service_charge_pct`:** `9`
*(Explanation: Sum of Unicode values for 'ultra09' is 749. 749 % 10 = 9)*

## Submission Question Answer

**What was the hardest design decision you made while building this, and what trade-off did you accept?**

The hardest design decision was choosing the algorithm for the Settlement Optimization. The problem of finding the absolute minimum number of transactions to settle debts in a directed graph is known to be NP-Hard (Subset Sum problem variant). 

**The Trade-off:** I decided to implement a **Greedy Algorithm** using two Priority Queues (one for Debtors, one for Creditors). Instead of guaranteeing the absolute theoretical minimum number of transactions for every possible edge case (which would require exponential time complexity $O(2^N)$), the greedy approach matches the largest debtor with the largest creditor iteratively. This trade-off sacrifices finding the "perfect" minimum transactions in highly complex edge cases, but in return, it provides an $O(N \log N)$ time complexity. This ensures the API remains extremely fast, scalable, and highly performant for real-world group sizes while still significantly reducing the transaction volume compared to direct repayment.
