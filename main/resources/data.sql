-- ==========================================================
-- SmartPark preloaded reference / dummy data
-- Loaded automatically on startup (spring.sql.init.mode=always)
-- ==========================================================

-- Parking Lots
INSERT INTO parking_lot (lot_id, location, capacity, occupied_spaces, cost_per_minute) VALUES
  ('LOT-001', 'Downtown Plaza',       50, 0, 0.50);
INSERT INTO parking_lot (lot_id, location, capacity, occupied_spaces, cost_per_minute) VALUES
  ('LOT-002', 'Airport Terminal 1',  100, 0, 0.75);
INSERT INTO parking_lot (lot_id, location, capacity, occupied_spaces, cost_per_minute) VALUES
  ('LOT-003', 'City Mall',            30, 0, 0.40);
INSERT INTO parking_lot (lot_id, location, capacity, occupied_spaces, cost_per_minute) VALUES
  ('LOT-004', 'Tiny Test Lot',         1, 0, 1.00);

-- Vehicles (registered, not yet checked in)
INSERT INTO vehicle (license_plate, type, owner_name) VALUES
  ('ABC-123', 'CAR',        'John Doe');
INSERT INTO vehicle (license_plate, type, owner_name) VALUES
  ('XYZ-789', 'MOTORCYCLE', 'Jane Smith');
INSERT INTO vehicle (license_plate, type, owner_name) VALUES
  ('TRK-456', 'TRUCK',      'Robert Brown');
INSERT INTO vehicle (license_plate, type, owner_name) VALUES
  ('MNO-321', 'CAR',        'Alice Green');
