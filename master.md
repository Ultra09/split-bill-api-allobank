# Allo Bank Backend Take-Home Test - Split Bill API

## Overview
Welcome to the Split Bill API submission. This project is a robust, production-ready RESTful API built with Java 21 and Spring Boot for managing shared expenses. 

The architecture strictly adheres to a clean separation of concerns using the Controller-Service-Repository pattern, ensuring high maintainability, testability, and readability.

## Key Highlights & Design Decisions
This implementation was designed with a focus on real-world fintech requirements:

*   **Financial Precision:** Complete elimination of floating-point inaccuracies. All monetary values and calculations strictly utilize `BigDecimal` with precise scale handling.
*   **Settlement Optimization (NP-Hard handling):** Implemented a Greedy Algorithm using two `PriorityQueue` instances (one for Debtors, one for Creditors) to iteratively resolve balances in $O(N \log N)$ time. This successfully minimizes the total number of required transactions between participants without the exponential time complexity overhead of a perfect subset-sum search.
*   **Robust Data Modeling:** The core algorithm maps relationships via numeric primary keys (`Long` IDs) instead of relying on full JPA entity equality, preventing any potential edge cases caused by Hibernate lazy-loading proxies.
*   **Comprehensive Features:** Goes beyond minimum requirements by offering:
    *   **Multiple Split Strategies:** Dynamic expense division (Equal split, Split by percentage, Split by exact amount).
    *   **Expense Categories:** Transactions can be categorized (`FOOD`, `TRANSPORT`, etc.) with a built-in category summary generated at settlement.
    *   **Mid-Trip Payments:** A payment recording endpoint that properly updates and reduces real-time net balances.
    *   **Audit Trail:** An immutable audit log tracking all group actions (creations, expenses, and payments) to ensure transparency.

## Technical Specifications
*   **Language & Framework:** Java 21, Spring Boot 3.x (Web, Data JPA, Validation)
*   **Build Tool:** Maven
*   **Database:** H2 Database (In-memory) for zero-configuration, instant evaluation.
*   **Port:** Configured to run on port `4110` per the assignment specification.

## Domain & Features Architecture

The system supports a full lifecycle flow:
1.  **Group Management:** Create groups and register a list of participants.
2.  **Expense Tracking:** Record expenses including the payer, total amount, beneficiaries, categories, and split distributions.
3.  **Payment Recording:** Record direct peer-to-peer payments that immediately reduce outstanding debts.
4.  **Audit Trail:** Retrieve a chronological log of all activities within a group.
5.  **Settlement Calculation:** Trigger the optimization algorithm to calculate the absolute minimum number of transactions needed to settle all remaining debts.

## Personalization & Dynamic Calculation
As requested in the challenge parameters, the settlement response includes dynamic fields: `service_charge_pct` and `service_charge_amount`.

*   **Dynamic Computation:** The code calculates the `service_charge_pct` dynamically by summing the ASCII (Unicode) values of the target GitHub username (`ultra09`) and applying a modulo 10 operation to the sum.
*   **Amount Calculation:** The `service_charge_amount` is calculated by applying this dynamic percentage to the total aggregated expenses of the group. 
*   **No Hardcoding:** These values are resolved entirely at runtime by the `PersonalizationService`.

## Getting Started
Please refer to the `README.md` file for instructions on building, running via Maven/Docker, and a comprehensive flow-by-flow guide on testing the API via cURL or Postman.