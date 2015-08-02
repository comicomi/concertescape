--name:get-airport-by-city-and-country
-- retrieve specific airport
SELECT iatacode FROM airport2
WHERE city=:city AND country=:country

-- name:get-airports
-- retrieve all airports.
SELECT * FROM airport2 ORDER BY name ASC

--name:get-city-by-airport-code
-- retrieve specific city
SELECT city FROM airport2
WHERE iatacode=:iatacode

--name:get-coordinates
-- retrieve coordinates of a specific city
SELECT latitude, longitude FROM airport2
WHERE iatacode=:iatacode
