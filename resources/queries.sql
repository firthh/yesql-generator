-- name: get-tables
SHOW TABLES

-- name: select-clicks
SELECT id, product_type, product_id, ref, uswitch_visit_id, uswitch_customer_id, uswitch_request_id, traffic_type, referrer_url, position, ip, user_agent, created_at, source, utm_medium, utm_campaign, utm_source, utm_term, product_name, company_name, params, source_type, category, page, utm_gclid, device, last_ref_code, sort, uscv, usca, guid, redirect_url FROM clicks WHERE id=:id
