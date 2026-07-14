# Email Service

> **Framework:** Spring Boot 4.1 + Thymeleaf + JavaMailSender
> **Pattern:** Strategy Pattern with pluggable email providers
> **Templates:** Thymeleaf HTML with inline CSS (email client compatible)

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Key Components](#2-key-components)
3. [Thymeleaf Templates](#3-thymeleaf-templates)
4. [Email Types](#4-email-types)
5. [Email Templates](#5-email-templates)
6. [Notification Preferences](#6-notification-preferences)
7. [REST API Endpoints](#7-rest-api-endpoints)

---

## 2. Key Components

### EmailService Interface

| Method | Description |
|--------|-------------|
| `sendEmail(EmailMessage)` | Sends regular email, respects user preferences |
| `sendCriticalEmail(EmailMessage)` | Sends critical email, bypasses preferences |

### EmailServiceImpl Methods

| Method | Transaction | Description |
|--------|-----------|----------|
| `sendEmail(EmailMessage)` | read-write | Checks preferences, sends via provider, logs result |
| `sendCriticalEmail(EmailMessage)` | read-write | Skips preferences, sends immediately |
| `shouldSendEmail(Long, EmailType)` | - | Checks NotificationPreference for email type |
| `sendWithLogging(EmailMessage)` | - | Internal: selects provider, sends, logs status |

### EmailProvider Interface

| Method | Description |
|--------|-------------|
| `supports()` | Returns PaymentGateway enum value |
| `send(EmailMessage)` | Sends email via provider |
| `getProviderName()` | Returns provider name for logging |

### SmtpEmailProvider Methods

| Method | Description |
|--------|-------------|
| `send(EmailMessage)` | `@Async` Thymeleaf rendering, JavaMailSender |
| `exchange(HttpMethod, String, Object, Consumer)` | REST client calls to SMTP |

### EmailMessage DTO Fields

| Field | Type | Description |
|-------|------|-------------|
| `to` | String | Recipient email |
| `subject` | String | Email subject |
| `templateName` | String | Template to render |
| `variables` | Map | Thymeleaf variables |
| `emailType` | EmailType | Type for preference checking |
| `userId` | Long | User for preference lookup |

---

## 3. Thymeleaf Templates

| Template | Path | Purpose | Variables |
|----------|------|---------|-----------|
| `base.html` | `templates/emails/base.html` | Shared layout fragment | `content` |
| `welcome.html` | `templates/emails/welcome.html` | New user welcome | `name`, `username` |
| `verification.html` | `templates/emails/verification.html` | Email verification | `name`, `verificationCode` |
| `password-reset.html` | `templates/emails/password-reset.html` | Password reset | `name`, `resetCode` |
| `order-update.html` | `templates/emails/order-update.html` | Order status change | `name`, `orderStatus`, `orderId` |
| `security-alert.html` | `templates/emails/security-alert.html` | Security notification | `name`, `alertMessage` |
| `promotion.html` | `templates/emails/promotion.html` | Marketing email | `name`, `promoCode`, `discount` |
| `review-engagement.html` | `templates/emails/review-engagement.html` | Review request | `name`, `itemName`, `reviewUrl` |

---

## 4. Email Types

### EmailType Enum

| Enum Value | Template | Preference Check | Used By |
|-----------|----------|-----------------|---------|
| `VERIFICATION` | verification | `accountSecurity` | EmailAuthController |
| `PASSWORD_RESET` | password-reset | `accountSecurity` | EmailAuthController |
| `SECURITY_ALERT` | security-alert | `accountSecurity` | Security events |
| `ORDER_UPDATE` | order-update | `orderUpdates` | OrderEmailService |
| `WELCOME` | welcome | None (always sent) | UserService on registration |
| `PROMOTION` | promotion | `promotions` | AdminMailService |
| `REVIEW_ENGAGEMENT` | review-engagement | `reviewsEngagement` | Review triggers |
| `ADMIN_MAIL` | promotion | None (always sent) | AdminMailService |

---

## 5. Email Templates

### Template Details

| Template | Variables | Purpose |
|----------|-----------|---------|
| `base.html` | `subject`, `content` | Shared layout wrapper (header, footer) |
| `welcome.html` | `userName` | New user welcome with features list |
| `verification.html` | `userName`, `verificationCode` | Email verification (10-min expiry) |
| `password-reset.html` | `userName`, `verificationCode` | Password reset code (10-min expiry) |
| `order-update.html` | `userName`, `orderId`, `orderStatus`, `orderTitle`, `orderMessage`, `orderTotal`, `isDelivered`, `deliveryAddress` | Order status notification |
| `security-alert.html` | `userName`, `alertTitle`, `alertMessage`, `alertType`, `alertTime`, `ipAddress` | Security event notification |
| `promotion.html` | `userName`, `title`, `body`, `ctaText`, `ctaUrl`, `unsubscribeUrl` | Marketing email |
| `review-engagement.html` | `userName`, `title`, `body`, `ctaText`, `ctaUrl` | Review request |

### Template Styling

| Template | Header Color | Border/Highlight |
|----------|--------------|------------------|
| `base.html` | `#4f46e5` (indigo) | - |
| `welcome.html` | `#4f46e5` (indigo) | - |
| `verification.html` | `#4f46e5` (indigo) | Code: `#f1f5f9` bg |
| `password-reset.html` | `#4f46e5` (indigo) | Code: `#fef2f2` bg, `#ef4444` border |
| `order-update.html` | `#059669` (green) | Details: `#f0fdf4` bg |
| `security-alert.html` | `#dc2626` (red) | Alert: `#fef2f2` bg, `#fecaca` border |
| `promotion.html` | `#2d7d46` (green) | - |
| `review-engagement.html` | `#4f46e5` (indigo) | - |
