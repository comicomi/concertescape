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

--name:get-airport
-- retrieve specific airport
SELECT iatacode FROM airport2 WHERE
  CASE (SELECT count(iatacode) FROM airport2 WHERE name='All Airports' AND city=:city AND country=:country)
   WHEN 1 THEN  name='All Airports' AND city=:city AND country=:country
   ELSE city=:city AND country=:country
  END
