# Despliegue en AWS EC2

Pasos para dejar el backend (Spring Boot + PostgreSQL con Docker) corriendo en una instancia EC2.
Es el mismo `docker-compose.yml` que usamos en local, así el entorno es igual en todos lados.

## 1. Crear la instancia

1. Consola de AWS → **EC2** → **Launch instance**.
2. Nombre: `veterinaria-backend`.
3. AMI: **Amazon Linux 2023**.
4. Tipo: **t2.micro** (o t3.micro), capa gratuita: 1 vCPU y 1 GB de RAM.
5. Key pair: crear uno nuevo y descargar el `.pem` (es la llave para entrar por SSH, AWS no la guarda).
6. **Security Group** (firewall de la instancia), reglas de entrada:

| Tipo | Puerto | Origen | Para qué |
| --- | --- | --- | --- |
| SSH | 22 | Mi IP | Entrar a la máquina |
| TCP personalizado | 8080 | 0.0.0.0/0 | La API |

No se abre el 5432: la base de datos solo la usa el backend, por la red interna de Docker.

7. Almacenamiento: 8 a 16 GB. **Launch instance**.

## 2. Conectarse

```
ssh -i "veterinaria.pem" ec2-user@IP_PUBLICA
```

(En Linux/Mac antes: `chmod 400 veterinaria.pem`.)

## 3. Instalar Docker y Git

```
sudo dnf update -y
sudo dnf install -y docker git
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user

# plugin "docker compose"
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# memoria swap: la t2.micro tiene 1 GB y Maven necesita más para compilar
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

exit
```

Volver a entrar por SSH (para que tome el grupo `docker`) y revisar: `docker compose version`.

## 4. Levantar el backend

```
git clone https://github.com/briveraac/EvP_veterinaria_backend.git
cd EvP_veterinaria_backend
cp .env.example .env
nano .env        # cambiar JWT_SECRETO y DB_PASSWORD, y agregar el origen del frontend a CORS_ORIGENES
docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

## 5. Probar con Postman

1. `POST http://IP_PUBLICA:8080/api/v1/auth/login`
   Body → raw → JSON: `{"email":"brun.rivera@duocuc.cl","password":"SanMarcos2026!"}`
2. Copiar el `token`.
3. En las siguientes peticiones: pestaña **Authorization** → tipo **Bearer Token** → pegar el token.
4. `GET http://IP_PUBLICA:8080/api/v1/citas` (200), `POST` (201), `PUT` (200), `DELETE` (204).
5. Sin token → 401. Con el token de `recepcion@sanmarcos.cl` el `DELETE` → 403.

Swagger: `http://IP_PUBLICA:8080/swagger-ui/index.html`

## 6. Conectar el frontend

En el proyecto del frontend crear `.env`:

```
VITE_API_URL=http://IP_PUBLICA:8080
```

y volver a compilar (`npm run build`), porque Vite pone esa dirección dentro del código al compilar.

En el `.env` del backend, agregar el origen del frontend a `CORS_ORIGENES` y reiniciar: `docker compose up -d`.

Nota: si el frontend está en HTTPS (GitHub Pages) y la API en HTTP, el navegador bloquea la llamada (*mixed content*). Para eso la API necesita HTTPS (Nginx con certificado o un Load Balancer con ACM).

## 7. Problemas comunes

| Problema | Causa | Solución |
| --- | --- | --- |
| Postman se queda cargando | Puerto 8080 cerrado en el Security Group | Agregar la regla de entrada |
| Connection refused | El contenedor no está corriendo | `docker compose ps` y `docker compose logs backend` |
| El build se cae | Poca RAM en la t2.micro | Activar la swap del paso 3 |
| Error de CORS en el navegador | El origen no está en `CORS_ORIGENES` | Agregarlo en `.env` y reiniciar |
| Cambió la IP | La IP pública cambia al detener la instancia | Usar una **Elastic IP** |

## 8. Al terminar

- **Stop**: apaga la instancia y conserva el disco (se puede volver a encender).
- **Terminate**: borra la instancia y su disco.
