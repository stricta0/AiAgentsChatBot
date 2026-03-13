# Troubleshooting Guide

This document describes common problems encountered when using the platform and how to resolve them.

It includes authentication problems, webhook errors, integration issues, and deployment problems.

If a problem is not covered in this document, contact the technical support team or consult the integration guide.

---

## Authentication Errors

Authentication errors are the most common issues encountered when interacting with the API.

They usually occur when the request is missing the correct authorization header, when the API token is invalid, or when the token has expired.

### HTTP 401 Unauthorized

HTTP 401 indicates that the request is not authenticated.

Common causes include:

- Missing Authorization header
- Invalid API token
- Expired API token
- Incorrect token format

To resolve this issue:

1. Ensure the Authorization header is present.
2. Verify the API token.
3. Ensure the token has not expired.
4. Ensure the header follows this format:

Authorization: Bearer <API_TOKEN>

If the problem persists, generate a new API token in the developer dashboard and retry the request.

### HTTP 403 Forbidden

HTTP 403 means that authentication succeeded but the user does not have permission to access the requested resource.

Possible causes:

- Missing required permissions
- Accessing an admin-only endpoint
- Organization-level restrictions

Steps to resolve:

1. Verify the permissions assigned to the API token.
2. Ensure the endpoint supports your role.
3. Contact your administrator if additional permissions are required.

---

## Webhook Delivery Failures

Webhook delivery issues can occur when the receiving server is unavailable or returns an unexpected response.

Typical symptoms include delayed webhook events, repeated retries, or dropped notifications.

Webhook requests expect a 200 response from the receiving server.

If the receiving server returns a non-200 response, the webhook system will retry delivery several times.

### Common Causes

The most frequent causes of webhook failures include:

- The receiving server is offline.
- The webhook endpoint URL is incorrect.
- The receiving server takes too long to respond.
- Network firewalls block incoming requests.

### Recommended Fix

Verify that the webhook endpoint:

- Is publicly accessible
- Responds within 5 seconds
- Returns HTTP 200 on success

You can test webhook endpoints using curl:

curl -X POST https://your-webhook-endpoint.com/webhook

---

## Integration Errors

Integration errors often occur during initial setup.

The most common problems include incorrect API endpoints, invalid credentials, or configuration mismatches.

Ensure that:

- The base API URL is correct
- The API token is valid
- Required environment variables are configured

Integration errors can also occur when request payloads are malformed.

Always verify the JSON payload structure when debugging integration issues.

---

## Deployment Problems

Deployment issues may occur when environment variables are missing or misconfigured.

Verify that the following variables are defined:

- API_TOKEN
- DATABASE_URL
- WEBHOOK_SECRET

Failure to configure these values may prevent the service from starting correctly.

If the application fails to start, inspect the startup logs and verify that all required configuration values are present.
