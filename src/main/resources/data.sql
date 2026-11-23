-- Citas de ejemplo. CURRENT_DATE es la fecha de hoy.
INSERT INTO citas (mascota, especie, dueno, servicio, fecha, hora, veterinario, estado) VALUES
('Luna', 'Perro', 'Francisca Muñoz', 'Consulta general', FORMATDATETIME(CURRENT_DATE, 'yyyy-MM-dd'), '09:00', 'Dra. Sofía Herrera', 'Completada'),
('Milo', 'Gato', 'Matías González', 'Vacunación', FORMATDATETIME(CURRENT_DATE, 'yyyy-MM-dd'), '10:00', 'Dr. Nicolás Vega', 'Confirmada'),
('Simón', 'Conejo', 'Antonia Pérez', 'Control de peso', FORMATDATETIME(CURRENT_DATE, 'yyyy-MM-dd'), '11:30', 'Dra. Sofía Herrera', 'Pendiente'),
('Nala', 'Perro', 'Diego Silva', 'Desparasitación', FORMATDATETIME(CURRENT_DATE, 'yyyy-MM-dd'), '12:00', 'Dra. Paula Fuentes', 'Confirmada'),
('Coco', 'Ave', 'Antonia Pérez', 'Consulta general', FORMATDATETIME(CURRENT_DATE, 'yyyy-MM-dd'), '15:00', 'Dr. Nicolás Vega', 'Pendiente'),
('Milo', 'Gato', 'Matías González', 'Control de peso', FORMATDATETIME(DATEADD('DAY', 1, CURRENT_DATE), 'yyyy-MM-dd'), '09:30', 'Dra. Paula Fuentes', 'Pendiente');
