    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function addPlace() {
            const container = document.getElementById('placesContainer');
            const div = document.createElement('div');
            div.className = 'input-group mb-2';
            div.innerHTML = `
                <input type="text" class="form-control" name="places[]" required>
                <button type="button" class="btn btn-outline-danger" onclick="removePlace(this)">Remove</button>
            `;
            container.appendChild(div);
        }

        function removePlace(button) {
            const inputs = document.querySelectorAll('input[name="places[]"]');
            if (inputs.length > 2) {
                button.parentElement.remove();
            } else {
                alert('At least 2 places are required.');
            }
        }
    </script>
</body>
</html>
