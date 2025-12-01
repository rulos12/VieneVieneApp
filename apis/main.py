import uuid
import os
from fastapi.staticfiles import StaticFiles


from fastapi import FastAPI, File, Form, HTTPException, UploadFile, status
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import mysql.connector
from pydantic import BaseModel

from datetime import date, datetime, timedelta
from decimal import Decimal


app = FastAPI()

app.mount("/static", StaticFiles(directory="static"), name="static")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)


def get_connection():
    return mysql.connector.connect(
        host="localhost",
        user="root",
        password="",
        database="vienevienecar",
        port=3306
    )
    
    
class ReservaEstado(BaseModel):
    estado: str
    
    
class Usuario(BaseModel):
    nombre: str
    correo: str
    telefono: str 
    contraseña: str
    rol: str = "cliente"  # cliente o propietario

class Ubicacion(BaseModel):
    direccion: str
    ciudad: str
    estado: str
    codigo_postal: str
    latitud: float
    longitud: float
    
class RolUpdate(BaseModel):
    id_usuario: int
    rol: str
    
    
class EstacionamientoCompleto(BaseModel):
    id_usuario: int
    nombre: str
    descripcion: str
    precio_hora: float
    capacidad: int
    horario_apertura: str
    horario_cierre: str
    estado: str | None = "activo"
    lugares_ocupados: int | None = 0
    ubicacion: Ubicacion

class LoginUsuario(BaseModel):
    correo: str
    password: str

class Estacionamiento(BaseModel):
    id_usuario: int
    id_ubicacion: int
    nombre: str
    descripcion: str
    precio_hora: float
    capacidad: int
    horario_apertura: str  
    horario_cierre: str    
    estado: str = "activo"
    lugares_ocupados: int = 0
    
    
class Reserva(BaseModel):
    id_usuario: int
    id_estacionamiento: int
    id_vehiculo: int
    fecha_reserva: str
    hora_inicio: str
    hora_fin: str
    monto_total: float
    estado: str

    
    
class Vehiculo(BaseModel):
    id_usuario: int
    tipo: str
    modelo: str
    placas: str

    
class EstacionamientoUbicacion(BaseModel):
    id_estacionamiento: int
    id_usuario: int
    id_ubicacion: int
    nombre: str
    descripcion: str
    precio_hora: float
    capacidad: int
    horario_apertura: timedelta
    horario_cierre: timedelta
    estado: str
    direccion: str
    ciudad: str
    estado_ubicacion: str
    codigo_postal: str
    latitud: float
    longitud: float

class ReservaPropietario(BaseModel):
    id_reserva: int
    fecha_reserva: str
    hora_inicio: str
    hora_fin: str
    monto_total: float
    estado: str
    cliente: dict
    vehiculo: dict
    estacionamiento: dict



def serialize_row(row, columns):
    result = {}
    for idx, value in enumerate(row):
        if isinstance(value, (date, datetime)):
            value = value.isoformat()
        elif isinstance(value, Decimal):
            value = float(value)
        elif isinstance(value, timedelta):  
            value = value.total_seconds()  
        result[columns[idx]] = value
    return result


##Apis con la tabla estacionamientos

@app.post("/createEstacionamiento")
def create_estacionamiento(est: Estacionamiento):
    conn = get_connection()
    cur = conn.cursor()
    
    # Verificar si el estacionamiento ya existe
    cur.execute("SELECT id_estacionamiento FROM estacionamiento WHERE nombre = %s AND id_usuario = %s", 
                (est.nombre, est.id_usuario))
    if cur.fetchone():
        cur.close()
        conn.close()
        return {"error": "Ya existe un estacionamiento con este nombre para este usuario"}
    
    # Insertar el nuevo estacionamiento
    cur.execute("""
        INSERT INTO estacionamiento (id_usuario, id_ubicacion, nombre, descripcion, precio_hora, capacidad, 
                                   horario_apertura, horario_cierre, estado, lugares_ocupados, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
    """, (est.id_usuario, est.id_ubicacion, est.nombre, est.descripcion, est.precio_hora, 
          est.capacidad, est.horario_apertura, est.horario_cierre, est.estado, est.lugares_ocupados))
    
    conn.commit()
    estacionamiento_id = cur.lastrowid
    cur.close()
    conn.close()
    
    return {
        "message": "Estacionamiento creado correctamente",
        "id_estacionamiento": estacionamiento_id
    }


@app.post("/createEstacionamientoCompleto")
def create_estacionamiento_completo(datos: EstacionamientoCompleto):
    print (datos)
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # 1. Primero crear la ubicación
        cur.execute("""
            INSERT INTO ubicacion (direccion, ciudad, estado, codigo_postal, latitud, longitud)
            VALUES (%s, %s, %s, %s, %s, %s)
        """, (datos.ubicacion.direccion, datos.ubicacion.ciudad, datos.ubicacion.estado,
              datos.ubicacion.codigo_postal, datos.ubicacion.latitud, datos.ubicacion.longitud))
        
        ubicacion_id = cur.lastrowid
        
        # 2. Luego crear el estacionamiento
        cur.execute("""
            INSERT INTO estacionamiento (id_usuario, id_ubicacion, nombre, descripcion, precio_hora, 
                                       capacidad, horario_apertura, horario_cierre, estado, lugares_ocupados, created_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
        """, (datos.id_usuario, ubicacion_id, datos.nombre, datos.descripcion, datos.precio_hora,
              datos.capacidad, datos.horario_apertura, datos.horario_cierre, datos.estado, datos.lugares_ocupados))
        
        estacionamiento_id = cur.lastrowid
        conn.commit()
        
        return {
            "message": "Estacionamiento y ubicación creados correctamente",
            "id_estacionamiento": estacionamiento_id,
            "id_ubicacion": ubicacion_id
        }
        
    except Exception as e:
        print("ERROR SQL:", str(e))
        conn.rollback()
        return {"error": f"Error al crear estacionamiento: {str(e)}"}
    finally:
        cur.close()
        conn.close()
@app.put("/actualizarRol")
def actualizar_rol(data: RolUpdate):
    conn = get_connection()
    cur = conn.cursor()
    try:
        cur.execute("SELECT id_usuario FROM usuario WHERE id_usuario = %s AND deleted_at IS NULL",
                    (data.id_usuario,))
        row = cur.fetchone()
        if not row:
            raise HTTPException(status_code=404, detail="Usuario no encontrado")

        cur.execute("""
            UPDATE usuario 
            SET rol = %s, updated_at = NOW()
            WHERE id_usuario = %s
        """, (data.rol, data.id_usuario))

        conn.commit()
        return {"message": "Rol actualizado correctamente", "nuevo_rol": data.rol}

    except Exception as e:
        conn.rollback()
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        cur.close()
        conn.close()


@app.post("/createUbicacion")
def create_ubicacion(ubicacion: Ubicacion):
    conn = get_connection()
    cur = conn.cursor()
    
    # Verificar si la ubicación ya existe
    cur.execute("""
        SELECT id_ubicacion FROM ubicacion 
        WHERE latitud = %s AND longitud = %s
    """, (ubicacion.latitud, ubicacion.longitud))
    
    if cur.fetchone():
        cur.close()
        conn.close()
        return {"error": "Ya existe una ubicación con estas coordenadas"}
    
    # Insertar la nueva ubicación
    cur.execute("""
        INSERT INTO ubicacion (direccion, ciudad, estado, codigo_postal, latitud, longitud)
        VALUES (%s, %s, %s, %s, %s, %s)
    """, (ubicacion.direccion, ubicacion.ciudad, ubicacion.estado, 
          ubicacion.codigo_postal, ubicacion.latitud, ubicacion.longitud))
    
    conn.commit()
    ubicacion_id = cur.lastrowid
    cur.close()
    conn.close()
    
    return {
        "message": "Ubicación creada correctamente",
        "id_ubicacion": ubicacion_id
    }



@app.get("/getEstacionamientosUbicacion")
def get_estacionamientos_ubicacion():
    try:
        conn = get_connection()
        cur = conn.cursor()
        cur.execute("SELECT * FROM vista_estacionamientos")
        columns = [desc[0] for desc in cur.description]
        rows = cur.fetchall()

        data = []
        for row in rows:
            o = serialize_row(row, columns)

            if o.get("foto_principal"):
                path = o["foto_principal"].replace("\\", "/")
                o["foto_principal"] = f"http://192.168.1.79:8000/{path}"

            data.append(o)

        return JSONResponse(content=data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


from decimal import Decimal

@app.get("/getEstacionamientosPropietario/{id_usuario}")
def get_estacionamientos_propietario(id_usuario: int):
    print (id_usuario)
    try:
        conn = get_connection()
        cur = conn.cursor()

        cur.execute("""
            SELECT 
                e.id_estacionamiento,
                e.id_usuario,
                e.id_ubicacion,
                e.nombre,
                e.descripcion,
                e.precio_hora,
                e.capacidad,
                e.estado,
                e.lugares_ocupados,
                
                u.direccion,
                u.ciudad,
                u.estado AS estado_region,
                u.codigo_postal

            FROM estacionamiento e
            INNER JOIN ubicacion u ON e.id_ubicacion = u.id_ubicacion
            WHERE e.id_usuario = %s
        """, (id_usuario,))

        columns = [desc[0] for desc in cur.description]
        rows = cur.fetchall()

        data = []

        for row in rows:
            estacionamiento = serialize_row(row, columns)

            # Obtener foto principal
            cur.execute("""
                SELECT url_foto
                FROM estacionamiento_fotos
                WHERE id_estacionamiento = %s
                ORDER BY id_foto ASC
                LIMIT 1
            """, (estacionamiento["id_estacionamiento"],))

            foto = cur.fetchone()

            if foto:
                path = foto[0].replace("\\", "/")
                estacionamiento["foto_principal"] = f"http://192.168.1.79:8000/{path}"
            else:
                estacionamiento["foto_principal"] = None

            data.append(estacionamiento)

        return JSONResponse(content=data)

    except Exception as e:
        print(" ERROR API get_estacionamientos_propietario:", e)
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/getEstacionamientos")
def get_estacionamientos():
    try:
        conn = get_connection()
        cur = conn.cursor()
        cur.execute("SELECT * FROM estacionamiento WHERE deleted_at IS NULL")
        columns = [desc[0] for desc in cur.description]
        rows = cur.fetchall()
        data = [serialize_row(row, columns) for row in rows]
        cur.close()
        conn.close()
        return JSONResponse(content=data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/getEstacionamiento/{id_estacionamiento}")
def get_estacionamiento(id_estacionamiento: int):
    try:
        conn = get_connection()
        cur = conn.cursor()

        # Traemos los datos de la vista del estacionamiento
        query = "SELECT * FROM vista_estacionamientos WHERE id_estacionamiento = %s"
        cur.execute(query, (id_estacionamiento,))
        row = cur.fetchone()

        if not row:
            raise HTTPException(status_code=404, detail="Estacionamiento no encontrado")

        columns = [desc[0] for desc in cur.description]
        data = serialize_row(row, columns)

        # Obtener foto principal desde tabla estacionamiento_fotos
        cur.execute("""
            SELECT url_foto
            FROM estacionamiento_fotos
            WHERE id_estacionamiento = %s
            ORDER BY created_at DESC
            LIMIT 1
        """, (id_estacionamiento,))
        foto = cur.fetchone()

        if foto:
            path = foto[0].replace("\\", "/")
            data["foto_principal"] = f"http://192.168.1.79:8000/{path}"
        else:
            data["foto_principal"] = None

        cur.close()
        conn.close()
        print (data)

        return JSONResponse(content=data)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))




@app.post("/uploadFotoEstacionamiento")
async def upload_foto(id_estacionamiento: int = Form(...), archivo: UploadFile = File(...)):
    try:
        contenido = await archivo.read()
        nombre_archivo = f"{uuid.uuid4()}.jpg"
        carpeta = "static/estacionamientos"
        os.makedirs(carpeta, exist_ok=True)
        ruta = os.path.join(carpeta, nombre_archivo)

        with open(ruta, "wb") as f:
            f.write(contenido)

        conn = get_connection()
        cur = conn.cursor()
        cur.execute("""
            INSERT INTO estacionamiento_fotos (id_estacionamiento, url_foto)
            VALUES (%s, %s)
        """, (id_estacionamiento, ruta))
        conn.commit()
        cur.close()
        conn.close()
        return {"status": "ok", "url_foto": ruta}
    except Exception as e:
        return JSONResponse(status_code=500, content={"error": str(e)})


@app.put("/updateEstacionamiento/{id}")
def update_estacionamiento(id: int, est: Estacionamiento):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        UPDATE estacionamiento
        SET nombre=%s, descripcion=%s, precio_hora=%s, capacidad=%s, updated_at=NOW()
        WHERE idEstacionamiento=%s
    """, (est.nombre, est.descripcion, est.precio_hora, est.capacidad, id))
    conn.commit()
    cur.close()
    conn.close()
    return {"message": "Estacionamiento actualizado correctamente"}

# Eliminar lógico
@app.delete("/deleteEstacionamiento/{id}")
def delete_estacionamiento(id: int):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("UPDATE estacionamiento SET deleted_at=NOW() WHERE idEstacionamiento=%s", (id,))
    conn.commit()
    cur.close()
    conn.close()
    return {"message": "Estacionamiento eliminado correctamente"}



@app.post("/createReserva")
def create_reserva(reserva: Reserva):
    conn = get_connection()
    cur = conn.cursor()

    inicio = datetime.strptime(f"{reserva.fecha_reserva} {reserva.hora_inicio}", "%Y-%m-%d %H:%M")
    fin = datetime.strptime(f"{reserva.fecha_reserva} {reserva.hora_fin}", "%Y-%m-%d %H:%M")

    cur.execute("""
        SELECT capacidad 
        FROM estacionamiento 
        WHERE id_estacionamiento = %s
    """, (reserva.id_estacionamiento,))
    row = cur.fetchone()

    if not row:
        raise HTTPException(status_code=404, detail="Estacionamiento no encontrado")

    capacidad_total = row[0]
    cur.execute("""
        SELECT COUNT(*)
        FROM reserva
        WHERE id_estacionamiento = %s
        AND estado != 'cancelada'
        AND deleted_at IS NULL
        AND (
            %s < hora_fin
            AND %s > hora_inicio
        )
    """, (reserva.id_estacionamiento, inicio, fin))

    traslapes = cur.fetchone()[0]

    if traslapes >= capacidad_total:
        raise HTTPException(
            status_code=400,
            detail="No hay lugares disponibles para ese horario"
        )

    cur.execute("""
        SELECT COUNT(*)
        FROM reserva
        WHERE id_usuario = %s
        AND estado != 'cancelada'
        AND deleted_at IS NULL
        AND (
            %s < hora_fin
            AND %s > hora_inicio
        )
    """, (reserva.id_usuario, inicio, fin))

    reservas_usuario = cur.fetchone()[0]

    if reservas_usuario > 0:
        raise HTTPException(
            status_code=409,
            detail="El usuario ya tiene una reserva activa en ese horario"
        )

    cur.execute("""
        INSERT INTO reserva (
            id_estacionamiento,
            id_usuario,
            id_vehiculo,
            fecha_reserva,
            hora_inicio,
            hora_fin,
            monto_total,
            estado,
            created_at
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, NOW())
    """, (
        reserva.id_estacionamiento,
        reserva.id_usuario,
        reserva.id_vehiculo,
        reserva.fecha_reserva,
        inicio,
        fin,
        reserva.monto_total,
        reserva.estado
    ))

    cur.execute("SELECT LAST_INSERT_ID();")
    id_generado = cur.fetchone()[0]

    cur.execute("""
        UPDATE estacionamiento
        SET lugares_ocupados = COALESCE(lugares_ocupados, 0) + 1,
            updated_at = NOW()
        WHERE id_estacionamiento = %s
    """, (reserva.id_estacionamiento,))

    conn.commit()
    cur.close()
    conn.close()

    return {"id_reserva": id_generado}

@app.get("/getReservas")
def get_estacionamientos():
    try:
        conn = get_connection()
        cur = conn.cursor()
        cur.execute("SELECT * FROM reserva WHERE deleted_at IS NULL")
        columns = [desc[0] for desc in cur.description]
        rows = cur.fetchall()
        data = [serialize_row(row, columns) for row in rows]
        cur.close()
        conn.close()
        return JSONResponse(content=data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/getReservasHistorial/{id_usuario}")
def get_reservas_historial(id_usuario: int):
    try:
        conn = get_connection()
        cur = conn.cursor()

        sql = """
            SELECT * 
            FROM vista_reservas
            WHERE id_usuario = %s
            ORDER BY fecha_reserva DESC;
        """

        cur.execute(sql, (id_usuario,))
        columns = [desc[0] for desc in cur.description]
        rows = cur.fetchall()
        data = [serialize_row(row, columns) for row in rows]
        
        for o in data:
            if o.get("foto_principal"):
              path = o["foto_principal"].replace("\\", "/")
              o["foto_principal"] = f"http://192.168.1.79:8000/{path}"
        
        cur.close()
        conn.close()
        print (data)

        return JSONResponse(content=data)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/getReservaDetalle/{id_reserva}")
def get_reserva_detalle(id_reserva: int):
    try:
        conn = get_connection()
        cur = conn.cursor()

        sql = """
            SELECT
                r.id_reserva,
                r.hora_inicio,
                r.hora_fin,
                r.estado,

                -- Vehículo
                v.tipo,
                v.modelo,
                v.placas,

                -- Cliente
                u.nombre AS cliente_nombre,
                u.telefono AS cliente_telefono,

                -- Estacionamiento
                e.id_estacionamiento,
                e.nombre AS est_nombre,
                e.descripcion,
                e.precio_hora,
                e.capacidad,
                e.horario_apertura,
                e.horario_cierre,

                -- Ubicacion
                ub.direccion,
                ub.ciudad,
                ub.estado AS estado_ubicacion,
                ub.codigo_postal,
                ub.latitud,
                ub.longitud,

                -- Propietario del estacionamiento
                du.nombre AS propietario_nombre

            FROM reserva r
            JOIN usuario u ON u.id_usuario = r.id_usuario
            JOIN vehiculo v ON v.id_vehiculo = r.id_vehiculo
            JOIN estacionamiento e ON e.id_estacionamiento = r.id_estacionamiento
            JOIN usuario du ON du.id_usuario = e.id_usuario
            JOIN ubicacion ub ON ub.id_ubicacion = e.id_ubicacion

            WHERE r.id_reserva = %s;
        """

        cur.execute(sql, (id_reserva,))
        row = cur.fetchone()

        if not row:
            cur.close()
            conn.close()
            raise HTTPException(status_code=404, detail="Reserva no encontrada")

        columns = [desc[0] for desc in cur.description]
        data = serialize_row(row, columns)

        # Obtener fotos del estacionamiento desde la tabla estacionamiento_fotos
        id_est = data.get("id_estacionamiento")
        fotos = []
        if id_est:
            cur.execute("""
                SELECT url_foto 
                FROM estacionamiento_fotos 
                WHERE id_estacionamiento = %s
                ORDER BY created_at ASC
            """, (id_est,))
            rows_fotos = cur.fetchall()
            if rows_fotos:
                for r in rows_fotos:
                    url = r[0]
                    if url:
                        # normalizar barras y agregar host base (igual que en otras APIs)
                        path = str(url).replace("\\", "/").lstrip("/")
                        full_url = f"http://192.168.1.79:8000/{path}"
                        fotos.append(full_url)

        # Añadir lista de fotos al resultado (vacío si no hay)
        data["fotos"] = fotos
        print (data)
        cur.close()
        conn.close()
        return JSONResponse(content=data)

    except Exception as e:
        # para depuración imprime el error en consola (ya tienes prints en otros endpoints)
        print("ERROR get_reserva_detalle:", str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/getReserva/{id_reserva}")
def get_estacionamiento(id_reserva: int):
    try:
        conn = get_connection()
        cur = conn.cursor()
        query = "SELECT * FROM reserva WHERE id_reserva = %s"
        cur.execute(query, (id_reserva,))
        row = cur.fetchone()

        if not row:
            raise HTTPException(status_code=404, detail="Estacionamiento no encontrado")

        columns = [desc[0] for desc in cur.description]
        data = serialize_row(row, columns)
        cur.close()
        conn.close()
        return JSONResponse(content=data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    
    
@app.get("/getReservasPropietario/{id_usuario}")
def get_reservas_propietario(id_usuario: int):
    
    print(id_usuario)
    try:
        conn = get_connection()
        cur = conn.cursor()

        query = """
            SELECT 
                r.id_reserva,
                r.fecha_reserva,
                r.hora_inicio,
                r.hora_fin,
                r.monto_total,
                r.estado,

                -- Usuario (cliente)
                u.id_usuario AS id_cliente,
                u.nombre AS nombre_cliente,

                -- Vehículo
                v.id_vehiculo,
                v.tipo AS tipo_vehiculo,
                v.modelo AS modelo_vehiculo,
                v.placas,

                -- Estacionamiento
                e.id_estacionamiento,
                e.nombre AS nombre_estacionamiento,
                e.descripcion AS descripcion_estacionamiento,
                e.precio_hora,

                -- Foto principal
                (
                    SELECT url_foto 
                    FROM estacionamiento_fotos 
                    WHERE id_estacionamiento = e.id_estacionamiento
                    ORDER BY created_at DESC 
                    LIMIT 1
                ) AS foto_principal

            FROM reserva r
            INNER JOIN usuario u ON r.id_usuario = u.id_usuario
            INNER JOIN vehiculo v ON r.id_vehiculo = v.id_vehiculo
            INNER JOIN estacionamiento e ON r.id_estacionamiento = e.id_estacionamiento
            
            -- FILTRAMOS SOLO ESTACIONAMIENTOS DEL PROPIETARIO
            WHERE e.id_usuario = %s AND r.estado != 'cancelada'

            ORDER BY r.fecha_reserva DESC, r.hora_inicio DESC
        """
        
        ##AND r.estado != 'cancelada'

        cur.execute(query, (id_usuario,))
        columns = [desc[0] for desc in cur.description]
        rows = cur.fetchall()

        data = []
        for row in rows:
            obj = serialize_row(row, columns)

            # Convertir ruta de la foto
            if obj.get("foto_principal"):
                path = obj["foto_principal"].replace("\\", "/")
                obj["foto_principal"] = f"http://192.168.1.79:8000/{path}"

            data.append(obj)
            
        print (data)

        cur.close()
        conn.close()

        return JSONResponse(content=data)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


    
@app.post("/vehiculo/registrar")
def registrar_vehiculo(v: Vehiculo):
    conn = get_connection()
    cur = conn.cursor()

    # Buscar si ya existe el vehículo con esas placas
    cur.execute("""
        SELECT id_vehiculo FROM vehiculo 
        WHERE placas = %s AND id_usuario = %s AND deleted_at IS NULL
    """, (v.placas, v.id_usuario))

    fila = cur.fetchone()

    if fila:
        # Ya existe → regresamos su ID
        return {"id_vehiculo": fila[0], "nuevo": False}

    # Insertar si no existe
    cur.execute("""
        INSERT INTO vehiculo (
            id_usuario, tipo, modelo, placas, created_at
        ) VALUES (%s, %s, %s, %s, NOW())
    """, (v.id_usuario, v.tipo, v.modelo, v.placas))

    conn.commit()

    # Obtener ID insertado
    cur.execute("SELECT LAST_INSERT_ID()")
    id_creado = cur.fetchone()[0]

    cur.close()
    conn.close()

    return {"id_vehiculo": id_creado, "nuevo": True}

    
@app.put("/cancelarReserva/{id}")
def cancelar_reserva(id: int, data: ReservaEstado):
    print(id, data)
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # Primero obtener el id_estacionamiento de la reserva
        cur.execute("""
            SELECT id_estacionamiento, estado 
            FROM reserva 
            WHERE id_reserva = %s
        """, (id,))
        
        reserva_actual = cur.fetchone()
        if not reserva_actual:
            raise HTTPException(status_code=404, detail="Reserva no encontrada")
        
        id_estacionamiento, estado_actual = reserva_actual
        
        # Actualizar el estado de la reserva
        cur.execute("""
            UPDATE reserva
            SET estado=%s, updated_at=NOW()
            WHERE id_reserva=%s
        """, (data.estado, id))
        
        # Si se está cancelando una reserva que no estaba cancelada antes, actualizar capacidad
        if data.estado == 'cancelada' and estado_actual != 'cancelada':
            cur.execute("""
                UPDATE estacionamiento 
                SET lugares_ocupados = GREATEST(COALESCE(lugares_ocupados, 0) - 1, 0),
                    updated_at = NOW()
                WHERE id_estacionamiento = %s
            """, (id_estacionamiento,))
        
        conn.commit()
        cur.close()
        conn.close()
        
        return {"message": "Reserva cancelada correctamente"}
        
    except Exception as e:
        conn.rollback()
        cur.close()
        conn.close()
        raise HTTPException(status_code=500, detail=f"Error al cancelar reserva: {str(e)}")
    
    
@app.put("/actualizarReserva/{id}")
def actualizar_reserva(id: int, data: ReservaEstado):
    print(id, data)
    conn = get_connection()
    cur = conn.cursor()

    try:
        # Obtener estado actual y estacionamiento
        cur.execute("""
            SELECT id_estacionamiento, estado 
            FROM reserva 
            WHERE id_reserva = %s
        """, (id,))
        
        reserva_actual = cur.fetchone()
        if not reserva_actual:
            raise HTTPException(status_code=404, detail="Reserva no encontrada")
        
        id_estacionamiento, estado_actual = reserva_actual

        # Actualizar el estado
        cur.execute("""
            UPDATE reserva
            SET estado=%s, updated_at=NOW()
            WHERE id_reserva=%s
        """, (data.estado, id))

        # Si se confirma y antes NO estaba confirmada → incrementar ocupados
        if data.estado == 'confirmada' and estado_actual != 'confirmada':
            cur.execute("""
                UPDATE estacionamiento 
                SET lugares_ocupados = lugares_ocupados + 1,
                    updated_at = NOW()
                WHERE id_estacionamiento = %s
            """, (id_estacionamiento,))

        # Si se cancela y antes NO estaba cancelada → reducir ocupados
        if data.estado == 'cancelada' and estado_actual != 'cancelada':
            cur.execute("""
                UPDATE estacionamiento 
                SET lugares_ocupados = GREATEST(lugares_ocupados - 1, 0),
                    updated_at = NOW()
                WHERE id_estacionamiento = %s
            """, (id_estacionamiento,))

        conn.commit()
        cur.close()
        conn.close()

        return {"message": f"Reserva {data.estado} correctamente"}

    except Exception as e:
        conn.rollback()
        cur.close()
        conn.close()
        raise HTTPException(status_code=500, detail=f"Error al actualizar reserva: {str(e)}")

    
@app.put("/updateReserva/{id}")
def update_reserva(id: int, reserva: Reserva):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        UPDATE reserva
        SET idUsuario=%s, idEstacionamiento=%s, fecha_inicio=%s, fecha_fin=%s, estado=%s, updated_at=NOW()
        WHERE idReserva=%s
    """, (reserva.id_usuario, reserva.id_estacionamiento, reserva.fecha_inicio, reserva.fecha_fin, reserva.estado, id))
    conn.commit()
    cur.close()
    conn.close()
    return {"message": "Reserva actualizada correctamente"}

@app.delete("/deleteReserva/{id}")
def delete_reserva(id: int):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("UPDATE reserva SET deleted_at=NOW() WHERE idReserva=%s", (id,))
    conn.commit()
    cur.close()
    conn.close()
    return {"message": "Reserva eliminada correctamente"}    

##Estadisticas para obtener las reservas y las horaas de reservas
@app.get("/estadisticas/{cliente_id}")
def get_estadisticas_usuario(cliente_id: int):
    try:
        conn = get_connection()
        cur = conn.cursor()
        
        cur.execute("""
            SELECT COUNT(*) as total_reservas 
            FROM reserva 
            WHERE id_usuario = %s AND estado IN ('confirmada', 'completada')
        """, (cliente_id,))
        total_reservas_result = cur.fetchone()[0]
        total_reservas = int(total_reservas_result) if total_reservas_result else 0
        
        cur.execute("""
            SELECT SUM(TIMESTAMPDIFF(SECOND, hora_inicio, hora_fin) / 3600) as horas_totales
            FROM reserva 
            WHERE id_usuario = %s AND estado IN ('confirmada', 'completada')
        """, (cliente_id,))
        
        horas_totales_result = cur.fetchone()[0]
        # Convertir Decimal a float
        if isinstance(horas_totales_result, Decimal):
            horas_totales = float(round(horas_totales_result, 2))
        else:
            horas_totales = round(horas_totales_result, 2) if horas_totales_result else 0.0
        
        cur.close()
        conn.close()
        
        return JSONResponse(content={
            "cliente_id": cliente_id,
            "total_reservas": total_reservas,
            "horas_totales": horas_totales
        })
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    
@app.post("/registerUsuario")
def register_usuario(usuario: Usuario):
    print ("datos recibidos: ",usuario)
    try:
        conn = get_connection()
        cur = conn.cursor()

        cur.execute("SELECT * FROM usuario WHERE correo = %s AND deleted_at IS NULL", (usuario.correo,))
        if cur.fetchone():
            raise HTTPException(status_code=400, detail="El correo ya está registrado.")

        query = """
            INSERT INTO usuario (nombre, correo, telefono, contraseña, rol, created_at)
            VALUES (%s, %s, %s, %s, %s, NOW())
        """
        values = (usuario.nombre, usuario.correo, usuario.telefono, usuario.contraseña, usuario.rol)
        cur.execute(query, values)
        conn.commit()

        cur.close()
        conn.close()
        return JSONResponse(status_code=status.HTTP_201_CREATED, content={"message": "Usuario registrado correctamente."})
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.put("/updateUsuario/{id_usuario}")
def update_usuario(id_usuario: int, usuario: Usuario):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        UPDATE usuario
        SET correo=%s, password=%s, tipo=%s, updated_at=NOW()
        WHERE idUsuario=%s
    """, (usuario.correo, usuario.password, usuario.tipo, id_usuario))
    conn.commit()
    cur.close()
    conn.close()
    return {"message": "Usuario actualizado correctamente"}

@app.delete("/deleteUsuario/{id_usuario}")
def delete_usuario(id_usuario: int):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        UPDATE usuario SET deleted_at=NOW() WHERE idUsuario=%s
    """, (id_usuario,))
    conn.commit()
    cur.close()
    conn.close()
    return {"message": "Usuario eliminado correctamente"}

##Logear usuario
@app.post("/loginUsuario")
def login_usuario(data: LoginUsuario):
    print (LoginUsuario)
    try:
        conn = get_connection()
        cur = conn.cursor()
        query = "SELECT * FROM usuario WHERE correo = %s AND contraseña = %s AND deleted_at IS NULL"
        cur.execute(query, (data.correo, data.password))
        row = cur.fetchone()

        if not row:
            raise HTTPException(status_code=401, detail="Correo o contraseña incorrectos.")

        columns = [desc[0] for desc in cur.description]
        user_data = serialize_row(row, columns)

        cur.close()
        conn.close()
        return JSONResponse(content=user_data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    
    
 #Obtener un usuario   
@app.get("/getUsuario/{id_usuario}")
def get_usuario(id_usuario: int):
    try:
        conn = get_connection()
        cur = conn.cursor()
        query = "SELECT * FROM usuario WHERE id_usuario = %s AND deleted_at IS NULL"
        cur.execute(query, (id_usuario,))
        row = cur.fetchone()

        if not row:
            raise HTTPException(status_code=404, detail="Usuario no encontrado")

        columns = [desc[0] for desc in cur.description]
        data = serialize_row(row, columns)
        cur.close()
        conn.close()
        return JSONResponse(content=data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    
    
    