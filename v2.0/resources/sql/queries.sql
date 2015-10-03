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

--name:get-artist-by-name
-- retrieve artist by name
SELECT id FROM artist
WHERE name LIKE :name

--name:get-artist-by-id
-- retrieve artist by id
SELECT name FROM artist
WHERE id=:id

--name:store-loglikelihood!
-- store item-to-item recommendation based on loglikelihood similarity
INSERT INTO loglikelihood
(recommendedid, requestedid, similarity)
VALUES (:item2, :item1, :similarity)

--name:store-tanimoto!
-- store item-to-item recommendation based on tanimoto similarity
INSERT INTO tanimoto
(recommendedid, requestedid, similarity)
VALUES (:item2, :item1, :similarity)

--name:store-pearson!
-- store item-to-item recommendation based on pearson similarity
INSERT INTO pearson
(recommendedid, requestedid, similarity)
VALUES (:item2, :item1, :similarity)

--name:store-euclidean!
-- store item-to-item recommendation based on euclidean similarity
INSERT INTO euclidean
(recommendedid, requestedid, similarity)
VALUES (:item2, :item1, :similarity)
