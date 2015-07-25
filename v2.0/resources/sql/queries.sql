--name:get-airport-by-city-and-country
-- retrieve specific airport
SELECT iatacode FROM airport2
WHERE city=:city AND country=:country

-- name:get-airports
-- retrieve all airports.
SELECT * FROM airport2 ORDER BY name ASC

