const API_URL = "/api/tasks";

// HTML elementy
const taskForm = document.getElementById("task-form");
const tasksList = document.getElementById("tasks-list");
const statusMessage = document.getElementById("status-message");

const taskIdInput = document.getElementById("task-id");
const titleInput = document.getElementById("title");
const descriptionInput = document.getElementById("description");
const dueDateInput = document.getElementById("due-date");

const formTitle = document.getElementById("form-title");
const submitButton = document.getElementById("submit-button");
const cancelButton = document.getElementById("cancel-button");
const refreshButton = document.getElementById("refresh-button");

// Načtení všech úkolů
async function loadTasks() {
  try {
    statusMessage.textContent = "Načítám úkoly...";

    const response = await fetch(API_URL);

    if (!response.ok) {
      throw new Error("Nepodařilo se načíst úkoly.");
    }

    const tasks = await response.json();

    renderTasks(tasks);

    statusMessage.textContent = "";
  } catch (error) {
    statusMessage.textContent = error.message;
  }
}

// Vykreslení úkolů do HTML
function renderTasks(tasks) {
  tasksList.replaceChildren();

  if (tasks.length === 0) {
    const message = document.createElement("p");
    message.className = "empty-message";
    message.textContent = "Zatím nemáš žádné úkoly.";

    tasksList.appendChild(message);
    return;
  }

  tasks.forEach((task) => {
    const card = document.createElement("article");
    card.className = "task-card";

    if (task.completed) {
      card.classList.add("completed");
    }

    const title = document.createElement("h3");
    title.className = "task-title";
    title.textContent = task.title;

    const description = document.createElement("p");
    description.className = "task-description";
    description.textContent = task.description || "";

    const meta = document.createElement("p");
    meta.className = "task-meta";

    const dueDate = task.dueDate
      ? new Date(task.dueDate).toLocaleString("cs-CZ")
      : "Bez termínu";

    meta.textContent = `Termín: ${dueDate} | Stav: ${task.completed ? "Dokončeno" : "Nedokončeno"}`;

    const actions = document.createElement("div");
    actions.className = "task-actions";

    const completeButton = document.createElement("button");
    completeButton.textContent = task.completed
      ? "Označit jako nedokončené"
      : "Dokončit";

    completeButton.addEventListener("click", () => {
      toggleTaskCompletion(task);
    });

    const editButton = document.createElement("button");
    editButton.textContent = "Upravit";

    editButton.addEventListener("click", () => {
      editTask(task);
    });

    const deleteButton = document.createElement("button");
    deleteButton.textContent = "Smazat";
    deleteButton.className = "button-danger";

    deleteButton.addEventListener("click", () => {
      deleteTask(task.id);
    });

    actions.append(completeButton, editButton, deleteButton);

    card.append(title, description, meta, actions);

    tasksList.appendChild(card);
  });
}

// Vytvoření nebo aktualizace úkolu
taskForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const id = taskIdInput.value;

  const task = {
    title: titleInput.value.trim(),
    description: descriptionInput.value.trim(),
    completed: false,
    dueDate: dueDateInput.value ? `${dueDateInput.value}:00` : null,
  };

  try {
    const response = await fetch(id ? `${API_URL}/${id}` : API_URL, {
      method: id ? "PUT" : "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(task),
    });

    if (!response.ok) {
      throw new Error("Nepodařilo se uložit úkol.");
    }

    resetForm();
    await loadTasks();

    statusMessage.textContent = id ? "Úkol byl upraven." : "Úkol byl vytvořen.";
  } catch (error) {
    statusMessage.textContent = error.message;
  }
});

// Příprava formuláře pro úpravu úkolu
function editTask(task) {
  taskIdInput.value = task.id;
  titleInput.value = task.title;
  descriptionInput.value = task.description || "";

  dueDateInput.value = task.dueDate ? task.dueDate.substring(0, 16) : "";

  formTitle.textContent = "Upravit úkol";
  submitButton.textContent = "Uložit změny";
  cancelButton.hidden = false;

  window.scrollTo({
    top: 0,
    behavior: "smooth",
  });
}

// Odstranění úkolu
async function deleteTask(id) {
  if (!confirm("Opravdu chceš tento úkol odstranit?")) {
    return;
  }

  try {
    const response = await fetch(`${API_URL}/${id}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      throw new Error("Nepodařilo se odstranit úkol.");
    }

    await loadTasks();
    statusMessage.textContent = "Úkol byl odstraněn.";
  } catch (error) {
    statusMessage.textContent = error.message;
  }
}

// Změna stavu dokončení
async function toggleTaskCompletion(task) {
  const updatedTask = {
    title: task.title,
    description: task.description,
    completed: !task.completed,
    dueDate: task.dueDate,
  };

  try {
    const response = await fetch(`${API_URL}/${task.id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(updatedTask),
    });

    if (!response.ok) {
      throw new Error("Nepodařilo se změnit stav úkolu.");
    }

    await loadTasks();
  } catch (error) {
    statusMessage.textContent = error.message;
  }
}

// Vymazání formuláře
function resetForm() {
  taskForm.reset();
  taskIdInput.value = "";

  formTitle.textContent = "Nový úkol";
  submitButton.textContent = "Přidat úkol";
  cancelButton.hidden = true;
}

// Zrušení úpravy
cancelButton.addEventListener("click", resetForm);

// Obnovení seznamu
refreshButton.addEventListener("click", loadTasks);

// První načtení úkolů po otevření stránky
loadTasks();
