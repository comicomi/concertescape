--name:getairport-by-id
-- retrieve specific airport
SELECT * FROM airport2
WHERE iatacode = :iatacode

-- name:get-airports
-- retrieve all airports.
SELECT * FROM airport2 ORDER BY name ASC
