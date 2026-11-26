-- Citas de ejemplo para PostgreSQL. Solo se insertan si la tabla esta vacia.
INSERT INTO citas (mascota, especie, dueno, servicio, fecha, hora, veterinario, estado)
SELECT * FROM (VALUES
('Luna',  'Perro',  'Francisca Muñoz', 'Consulta general', to_char(CURRENT_DATE, 'YYYY-MM-DD'),     '09:00', 'Dra. Sofía Herrera', 'Completada'),
('Milo',  'Gato',   'Matías González', 'Vacunación',       to_char(CURRENT_DATE, 'YYYY-MM-DD'),     '10:00', 'Dr. Nicolás Vega',   'Confirmada'),
('Simón', 'Conejo', 'Antonia Pérez',   'Control de peso',  to_char(CURRENT_DATE, 'YYYY-MM-DD'),     '11:30', 'Dra. Sofía Herrera', 'Pendiente'),
('Nala',  'Perro',  'Diego Silva',     'Desparasitación',  to_char(CURRENT_DATE, 'YYYY-MM-DD'),     '12:00', 'Dra. Paula Fuentes', 'Confirmada'),
('Coco',  'Ave',    'Antonia Pérez',   'Consulta general', to_char(CURRENT_DATE, 'YYYY-MM-DD'),     '15:00', 'Dr. Nicolás Vega',   'Pendiente'),
('Milo',  'Gato',   'Matías González', 'Control de peso',  to_char(CURRENT_DATE + 1, 'YYYY-MM-DD'), '09:30', 'Dra. Paula Fuentes', 'Pendiente')
) AS v(mascota, especie, dueno, servicio, fecha, hora, veterinario, estado)
WHERE NOT EXISTS (SELECT 1 FROM citas);
